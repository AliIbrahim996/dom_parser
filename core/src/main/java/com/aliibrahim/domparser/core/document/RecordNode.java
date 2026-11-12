package com.aliibrahim.domparser.core.document;

import com.aliibrahim.domparser.core.schema.FieldDefinition;
import com.aliibrahim.domparser.core.schema.RecordSchema;
import org.jdom2.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A live view over one record element. Reads go straight to the underlying JDOM element, so it never goes stale
 * while the element is part of the document. Writes go through {@link XmlDocumentModel} so listeners are notified.
 */
public final class RecordNode {

    private final Element element;
    private final RecordSchema schema;

    /**
     * Creates a view over {@code element}; only {@link XmlDocumentModel} creates record nodes.
     *
     * @param element the record element, e.g. a {@code <User>}
     * @param schema  the schema describing the element
     */
    RecordNode(Element element, RecordSchema schema) {
        this.element = Objects.requireNonNull(element);
        this.schema = Objects.requireNonNull(schema);
    }

    /**
     * The underlying JDOM element.
     *
     * @return the record element itself (not a copy)
     */
    public Element element() {
        return element;
    }

    /**
     * The schema that describes this record.
     *
     * @return the record's schema
     */
    public RecordSchema schema() {
        return schema;
    }

    /**
     * The record id from the schema's id attribute.
     *
     * @return the id, or an empty string when the attribute is missing
     */
    public String id() {
        return element.getAttributeValue(schema.idAttribute(), "");
    }

    /**
     * Reads one field value.
     *
     * @param field the field (child element) name
     * @return the trimmed text of the field, or an empty string when the field is missing
     */
    public String get(String field) {
        Element child = element.getChild(field);
        return child == null ? "" : child.getTextTrim();
    }

    /**
     * Reads every schema field.
     *
     * @return field name to value, in schema order; missing fields map to an empty string
     */
    public Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        for (FieldDefinition field : schema.fields()) {
            values.put(field.name(), get(field.name()));
        }
        return values;
    }

    /**
     * Writes one field, creating the child element if needed. Package-private so that only
     * {@link XmlDocumentModel} can change records (and notify its listeners).
     *
     * @param field the field (child element) name
     * @param value the new value; {@code null} is stored as an empty string, other values are trimmed
     */
    void set(String field, String value) {
        Element child = element.getChild(field);
        if (child == null) {
            child = new Element(field);
            element.addContent(child);
        }
        child.setText(value == null ? "" : value.trim());
    }

    /**
     * A short label such as {@code "User 3"}.
     *
     * @return the record element name followed by the id
     */
    public String label() {
        return schema.recordElement() + " " + id();
    }

    /** Two record nodes are equal when they view the very same element instance. */
    @Override
    public boolean equals(Object o) {
        return o instanceof RecordNode other && other.element == element;
    }

    /** Identity hash of the underlying element, consistent with {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        return System.identityHashCode(element);
    }

    /** Label and values, for debugging. */
    @Override
    public String toString() {
        return label() + " " + values();
    }
}
