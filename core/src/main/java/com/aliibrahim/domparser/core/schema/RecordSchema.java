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

    /** Stable identifier, e.g. {@code "users"}. */
    String id();

    /** Name shown to people, e.g. {@code "Users"}. */
    String displayName();

    String rootElement();

    String recordElement();

    /** Attribute holding the numeric record id. */
    default String idAttribute() {
        return "id";
    }

    List<FieldDefinition> fields();

    /** Whether this schema understands {@code document}. The default matches on the root element name. */
    default boolean supports(Document document) {
        return document.hasRootElement() && document.getRootElement().getName().equals(rootElement());
    }

    /** Validates a full set of field values. Returns field name to error message; empty when valid. */
    default Map<String, String> validate(Map<String, String> values) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldDefinition field : fields()) {
            field.validate(values.get(field.name())).ifPresent(msg -> errors.put(field.name(), msg));
        }
        return errors;
    }

    default FieldDefinition field(String name) {
        return fields().stream()
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown field: " + name));
    }
}
