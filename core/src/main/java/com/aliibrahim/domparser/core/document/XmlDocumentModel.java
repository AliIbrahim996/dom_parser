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

    private XmlDocumentModel(Document document, Path file, RecordSchema schema) {
        this.document = Objects.requireNonNull(document);
        this.file = file;
        this.schema = schema;
        this.highestId = maxVisibleId();
    }

    /** Loads {@code file}, choosing a schema from {@code registry}; documents with no matching schema still open. */
    public static XmlDocumentModel open(Path file, SchemaRegistry registry) throws IOException, JDOMException {
        Document doc = XmlIO.read(file);
        return new XmlDocumentModel(doc, file, registry.detect(doc).orElse(null));
    }

    /** A new, empty, unsaved document for {@code schema}. */
    public static XmlDocumentModel create(RecordSchema schema) {
        XmlDocumentModel model = new XmlDocumentModel(new Document(new Element(schema.rootElement())), null, schema);
        model.dirty = true;
        return model;
    }

    /** Wraps an in-memory document (handy for tests and imports). */
    public static XmlDocumentModel of(Document document, RecordSchema schema) {
        return new XmlDocumentModel(document, null, schema);
    }

    // ---- state -------------------------------------------------------------------------------------------------

    public Document document() {
        return document;
    }

    public Optional<Path> file() {
        return Optional.ofNullable(file);
    }

    public Optional<RecordSchema> schema() {
        return Optional.ofNullable(schema);
    }

    public boolean isDirty() {
        return dirty;
    }

    public List<RecordNode> records() {
        if (schema == null || !document.hasRootElement()) {
            return List.of();
        }
        return document.getRootElement().getChildren(schema.recordElement()).stream()
                .map(e -> new RecordNode(e, schema))
                .toList();
    }

    /** The record that {@code element} is, or belongs to. */
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

    public void updateRecord(RecordNode record, Map<String, String> values) {
        requireValid(values);
        values.forEach(record::set);
        changed(DocumentChange.CONTENT);
    }

    public void deleteRecord(RecordNode record) {
        record.element().detach();
        changed(DocumentChange.STRUCTURE);
    }

    /** Sets the text of a leaf element (used by in-tree editing). Validates when it is a schema field. */
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

    /** Swaps in a transformed tree (e.g. after encryption). */
    public void replaceDocument(Document newDocument) {
        Objects.requireNonNull(newDocument);
        highestId = Math.max(highestId, maxVisibleId()); // ids of the outgoing tree, before they may be encrypted
        this.document = newDocument;
        highestId = Math.max(highestId, maxVisibleId());
        changed(DocumentChange.REPLACED);
    }

    public void saveAs(Path target) throws IOException {
        XmlIO.write(document, target);
        this.file = target;
        this.dirty = false;
        fire(DocumentChange.SAVED);
    }

    public void save() throws IOException {
        saveAs(file().orElseThrow(() -> new IllegalStateException("Document has no file yet; use saveAs")));
    }

    // ---- listeners ---------------------------------------------------------------------------------------------

    public void addListener(DocumentListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DocumentListener listener) {
        listeners.remove(listener);
    }

    private void changed(DocumentChange change) {
        dirty = true;
        fire(change);
    }

    private void fire(DocumentChange change) {
        listeners.forEach(l -> l.documentChanged(this, change));
    }

    // ---- helpers -----------------------------------------------------------------------------------------------

    private RecordSchema requireSchema() {
        if (schema == null) {
            throw new IllegalStateException("This document has no known schema; records cannot be edited");
        }
        return schema;
    }

    private void requireValid(Map<String, String> values) {
        Map<String, String> errors = requireSchema().validate(values);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private int nextId() {
        highestId = Math.max(highestId, maxVisibleId()) + 1;
        return highestId;
    }

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
