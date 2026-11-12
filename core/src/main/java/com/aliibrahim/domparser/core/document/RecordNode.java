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

    RecordNode(Element element, RecordSchema schema) {
        this.element = Objects.requireNonNull(element);
        this.schema = Objects.requireNonNull(schema);
    }

    public Element element() {
        return element;
    }

    public RecordSchema schema() {
        return schema;
    }

    public String id() {
        return element.getAttributeValue(schema.idAttribute(), "");
    }

    public String get(String field) {
        Element child = element.getChild(field);
        return child == null ? "" : child.getTextTrim();
    }

    public Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        for (FieldDefinition field : schema.fields()) {
            values.put(field.name(), get(field.name()));
        }
        return values;
    }

    void set(String field, String value) {
        Element child = element.getChild(field);
        if (child == null) {
            child = new Element(field);
            element.addContent(child);
        }
        child.setText(value == null ? "" : value.trim());
    }

    /** Short label such as {@code "User 3"}. */
    public String label() {
        return schema.recordElement() + " " + id();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RecordNode other && other.element == element;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(element);
    }

    @Override
    public String toString() {
        return label() + " " + values();
    }
}
