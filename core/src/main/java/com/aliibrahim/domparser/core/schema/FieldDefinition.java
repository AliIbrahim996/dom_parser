package com.aliibrahim.domparser.core.schema;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes one child element of a record, e.g. {@code <firstName>} of a {@code <User>}.
 *
 * @param name     the XML element name
 * @param label    human-readable label shown by clients
 * @param type     the value type
 * @param options  allowed values for {@link FieldType#CHOICE}; empty otherwise
 * @param required whether an empty value is rejected
 */
public record FieldDefinition(String name, String label, FieldType type, List<String> options, boolean required) {

    public FieldDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(type, "type");
        options = List.copyOf(options == null ? List.of() : options);
    }

    public static FieldDefinition text(String name, String label) {
        return new FieldDefinition(name, label, FieldType.TEXT, List.of(), true);
    }

    public static FieldDefinition integer(String name, String label) {
        return new FieldDefinition(name, label, FieldType.INTEGER, List.of(), true);
    }

    public static FieldDefinition choice(String name, String label, String... options) {
        return new FieldDefinition(name, label, FieldType.CHOICE, List.of(options), true);
    }

    /** A copy of this field that accepts empty values. */
    public FieldDefinition optional() {
        return new FieldDefinition(name, label, type, options, false);
    }

    /** @return an error message if {@code value} is not acceptable for this field. */
    public Optional<String> validate(String value) {
        String v = value == null ? "" : value.trim();
        if (v.isEmpty()) {
            return required ? Optional.of(label + " is required") : Optional.empty();
        }
        return switch (type) {
            case TEXT -> Optional.empty();
            case INTEGER -> v.matches("-?\\d{1,9}")
                    ? Optional.empty()
                    : Optional.of(label + " must be a whole number");
            case CHOICE -> options.stream().anyMatch(o -> o.equalsIgnoreCase(v))
                    ? Optional.empty()
                    : Optional.of(label + " must be one of " + String.join(", ", options));
        };
    }
}
