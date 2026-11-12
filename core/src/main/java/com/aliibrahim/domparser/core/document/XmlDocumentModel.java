package com.aliibrahim.domparser.core.document;

import com.aliibrahim.domparser.core.schema.FieldDefinition;
import com.aliibrahim.domparser.core.schema.RecordSchema;
import com.aliibrahim.domparser.core.schema.SchemaRegistry;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The single source of truth for an open XML document: the JDOM tree, the file it came from, the schema that
 * describes it (if any) and whether it has unsaved changes. All mutations go through this class so that
 * {@link DocumentListener}s (UI, autosave, ...) are always informed.
 */
public final class XmlDocumentModel {

    private Document document;
    private Path file;
    private final RecordSchema schema;
    private boolean dirty;
    /** Highest record id ever seen, so ids hidden by encryption are never handed out again. */
    private int highestId;
    private final List<DocumentListener> listeners = new CopyOnWriteArrayList<>();

    /** Use the {@code open}, {@code create} or {@code of} factories. */
    private XmlDocumentModel(Document document, Path file, RecordSchema schema) {
        this.document = Objects.requireNonNull(document);
        this.file = file;
        this.schema = schema;
        this.highestId = maxVisibleId();
    }

    /**
     * Loads {@code file} and picks a schema for it from {@code registry}. Documents with no matching schema
     * still open, as generic XML.
     *
     * @param file     the XML file to load
     * @param registry the known schemas
     * @return the loaded, unmodified document
     * @throws IOException   if the file cannot be read
     * @throws JDOMException if the file is not well-formed XML or contains a DOCTYPE
     */
    public static XmlDocumentModel open(Path file, SchemaRegistry registry) throws IOException, JDOMException {
        Document doc = XmlIO.read(file);
        return new XmlDocumentModel(doc, file, registry.detect(doc).orElse(null));
    }

    /**
     * Creates a new, empty document for {@code schema}. It has no file yet and counts as unsaved.
     *
     * @param schema the kind of document to create
     * @return the new document
     */
    public static XmlDocumentModel create(RecordSchema schema) {
        XmlDocumentModel model = new XmlDocumentModel(new Document(new Element(schema.rootElement())), null, schema);
        model.dirty = true;
        return model;
    }

    /**
     * Wraps a document that is already in memory (handy for tests and imports).
     *
     * @param document the JDOM document
     * @param schema   its schema, or {@code null} for generic XML
     * @return the model, not dirty and without a file
     */
    public static XmlDocumentModel of(Document document, RecordSchema schema) {
        return new XmlDocumentModel(document, null, schema);
    }

    // ---- state -------------------------------------------------------------------------------------------------

    /**
     * The current JDOM tree. Read it freely, but change it only through this model's methods.
     *
     * @return the current document
     */
    public Document document() {
        return document;
    }

    /**
     * The file this document was loaded from or last saved to.
     *
     * @return the file, or empty for a document that has never been saved
     */
    public Optional<Path> file() {
        return Optional.ofNullable(file);
    }

    /**
     * The schema detected for this document.
     *
     * @return the schema, or empty for generic XML
     */
    public Optional<RecordSchema> schema() {
        return Optional.ofNullable(schema);
    }

    /**
     * Whether there are changes that have not been saved.
     *
     * @return true after any change since the last save
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * The records directly under the root element.
     *
     * @return the records in document order; empty when there is no schema
     */
    public List<RecordNode> records() {
        if (schema == null || !document.hasRootElement()) {
            return List.of();
        }
        return document.getRootElement().getChildren(schema.recordElement()).stream()
                .map(e -> new RecordNode(e, schema))
                .toList();
    }

    /**
     * Finds the record that {@code element} is, or belongs to.
     *
     * @param element any element of the document, or {@code null}
     * @return the enclosing record, or empty when the element is outside any record or there is no schema
     */
    public Optional<RecordNode> recordOf(Element element) {
        if (schema == null || element == null || !document.hasRootElement()) {
            return Optional.empty();
        }
        Element root = document.getRootElement();
        for (Element e = element; e != null; e = e.getParentElement()) {
            if (e.getName().equals(schema.recordElement()) && e.getParentElement() == root) {
                return Optional.of(new RecordNode(e, schema));
            }
        }
        return Optional.empty();
    }

    // ---- mutations ---------------------------------------------------------------------------------------------

    /**
     * Validates {@code values} and appends a new record with the next free id. Fires
     * {@link DocumentChange#STRUCTURE}.
     *
     * @param values field name to value
     * @return the new record
     * @throws ValidationException   if the values do not satisfy the schema
     * @throws IllegalStateException if the document has no schema
     */
    public RecordNode addRecord(Map<String, String> values) {
        RecordSchema s = requireSchema();
        requireValid(values);
        Element element = new Element(s.recordElement());
        element.setAttribute(s.idAttribute(), String.valueOf(nextId()));
        RecordNode node = new RecordNode(element, s);
        for (FieldDefinition field : s.fields()) {
            node.set(field.name(), values.get(field.name()));
        }
        document.getRootElement().addContent(element);
        changed(DocumentChange.STRUCTURE);
        return node;
    }

    /**
     * Validates {@code values} and writes them into {@code record}. Fires {@link DocumentChange#CONTENT}.
     *
     * @param record the record to change
     * @param values field name to value
     * @throws ValidationException if the values do not satisfy the schema
     */
    public void updateRecord(RecordNode record, Map<String, String> values) {
        requireValid(values);
        values.forEach(record::set);
        changed(DocumentChange.CONTENT);
    }

    /**
     * Removes {@code record} from the document. Fires {@link DocumentChange#STRUCTURE}.
     *
     * @param record the record to remove
     */
    public void deleteRecord(RecordNode record) {
        record.element().detach();
        changed(DocumentChange.STRUCTURE);
    }

    /**
     * Sets the text of a leaf element (used by in-tree editing). Values of schema fields are validated first.
     * Fires {@link DocumentChange#CONTENT}.
     *
     * @param element the element to change
     * @param text    the new text; {@code null} is stored as an empty string, other values are trimmed
     * @throws ValidationException if {@code element} is a schema field and {@code text} is not valid for it
     */
    public void setElementText(Element element, String text) {
        recordOf(element)
                .filter(record -> element.getParentElement() == record.element())
                .flatMap(record -> schema.fields().stream().filter(f -> f.name().equals(element.getName())).findFirst())
                .flatMap(field -> field.validate(text))
                .ifPresent(msg -> {
                    throw new ValidationException(Map.of(element.getName(), msg));
                });
        element.setText(text == null ? "" : text.trim());
        changed(DocumentChange.CONTENT);
    }

    /**
     * Swaps in a transformed tree, e.g. after encryption or decryption. Fires {@link DocumentChange#REPLACED}.
     * Record ids seen in the old tree are never handed out again, even if the new tree hides them.
     *
     * @param newDocument the document that replaces the current one
     */
    public void replaceDocument(Document newDocument) {
        Objects.requireNonNull(newDocument);
        highestId = Math.max(highestId, maxVisibleId()); // ids of the outgoing tree, before they may be encrypted
        this.document = newDocument;
        highestId = Math.max(highestId, maxVisibleId());
        changed(DocumentChange.REPLACED);
    }

    /**
     * Writes the document to {@code target}, which becomes the document's file. Clears the dirty flag and fires
     * {@link DocumentChange#SAVED}.
     *
     * @param target the file to write
     * @throws IOException if writing fails; the existing file is left untouched
     */
    public void saveAs(Path target) throws IOException {
        XmlIO.write(document, target);
        this.file = target;
        this.dirty = false;
        fire(DocumentChange.SAVED);
    }

    /**
     * Writes the document back to {@link #file()}.
     *
     * @throws IOException           if writing fails
     * @throws IllegalStateException if the document has never been saved; use {@link #saveAs(Path)}
     */
    public void save() throws IOException {
        saveAs(file().orElseThrow(() -> new IllegalStateException("Document has no file yet; use saveAs")));
    }

    // ---- listeners ---------------------------------------------------------------------------------------------

    /**
     * Registers a listener for future changes.
     *
     * @param listener the listener to add
     */
    public void addListener(DocumentListener listener) {
        listeners.add(listener);
    }

    /**
     * Stops notifying {@code listener}.
     *
     * @param listener the listener to remove
     */
    public void removeListener(DocumentListener listener) {
        listeners.remove(listener);
    }

    /** Marks the document dirty and notifies listeners. */
    private void changed(DocumentChange change) {
        dirty = true;
        fire(change);
    }

    /** Notifies every listener of {@code change}. */
    private void fire(DocumentChange change) {
        listeners.forEach(l -> l.documentChanged(this, change));
    }

    // ---- helpers -----------------------------------------------------------------------------------------------

    /** Returns the schema, or throws {@link IllegalStateException} for generic XML. */
    private RecordSchema requireSchema() {
        if (schema == null) {
            throw new IllegalStateException("This document has no known schema; records cannot be edited");
        }
        return schema;
    }

    /** Throws {@link ValidationException} unless {@code values} satisfy the schema. */
    private void requireValid(Map<String, String> values) {
        Map<String, String> errors = requireSchema().validate(values);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    /** Reserves and returns the next record id: one more than the highest id ever seen. */
    private int nextId() {
        highestId = Math.max(highestId, maxVisibleId()) + 1;
        return highestId;
    }

    /** Highest numeric id among the records currently readable (non-numeric ids count as 0). */
    private int maxVisibleId() {
        return records().stream()
                .mapToInt(r -> {
                    try {
                        return Integer.parseInt(r.id());
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
    }
}
