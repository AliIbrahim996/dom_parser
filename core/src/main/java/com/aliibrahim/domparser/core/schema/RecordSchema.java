package com.aliibrahim.domparser.core.schema;

import org.jdom2.Document;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a "record oriented" XML vocabulary such as:
 *
 * <pre>{@code
 * <Users>                 <- rootElement
 *   <User id="1">         <- recordElement + idAttribute
 *     <firstName>..       <- fields
 * }</pre>
 *
 * <p>To support a new kind of document, implement this interface and list the class in
 * {@code META-INF/services/com.aliibrahim.domparser.core.schema.RecordSchema}. {@link SchemaRegistry} then picks it
 * up automatically and the desktop client builds its table and form from it.
 */
public interface RecordSchema {

    /**
     * A stable identifier, e.g. {@code "users"}.
     *
     * @return the schema id
     */
    String id();

    /**
     * The name shown to people, e.g. {@code "Users"}.
     *
     * @return the display name
     */
    String displayName();

    /**
     * The name of the document's root element, e.g. {@code "Users"}.
     *
     * @return the root element name
     */
    String rootElement();

    /**
     * The name of each record element under the root, e.g. {@code "User"}.
     *
     * @return the record element name
     */
    String recordElement();

    /**
     * The attribute holding the numeric record id.
     *
     * @return the attribute name; {@code "id"} by default
     */
    default String idAttribute() {
        return "id";
    }

    /**
     * The fields of a record, in display order.
     *
     * @return the field definitions
     */
    List<FieldDefinition> fields();

    /**
     * Whether this schema understands {@code document}. The default matches on the root element name.
     *
     * @param document the loaded document
     * @return true when this schema should be used for it
     */
    default boolean supports(Document document) {
        return document.hasRootElement() && document.getRootElement().getName().equals(rootElement());
    }

    /**
     * Validates a full set of field values.
     *
     * @param values field name to value; missing fields count as empty
     * @return field name to error message, in field order; empty when everything is valid
     */
    default Map<String, String> validate(Map<String, String> values) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldDefinition field : fields()) {
            field.validate(values.get(field.name())).ifPresent(msg -> errors.put(field.name(), msg));
        }
        return errors;
    }

    /**
     * Looks up a field by element name.
     *
     * @param name the field name
     * @return the field definition
     * @throws IllegalArgumentException if this schema has no such field
     */
    default FieldDefinition field(String name) {
        return fields().stream()
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + name));
    }
}
