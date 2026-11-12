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

    /** Checks the required components and makes {@code options} an unmodifiable copy. */
    public FieldDefinition {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(type, "type");
        options = List.copyOf(options == null ? List.of() : options);
    }

    /**
     * A required free-text field.
     *
     * @param name  the XML element name
     * @param label the label shown to people
     * @return the field definition
     */
    public static FieldDefinition text(String name, String label) {
        return new FieldDefinition(name, label, FieldType.TEXT, List.of(), true);
    }

    /**
     * A required whole-number field (up to 9 digits, optionally negative).
     *
     * @param name  the XML element name
     * @param label the label shown to people
     * @return the field definition
     */
    public static FieldDefinition integer(String name, String label) {
        return new FieldDefinition(name, label, FieldType.INTEGER, List.of(), true);
    }

    /**
     * A required field whose value must be one of {@code options} (compared case-insensitively).
     *
     * @param name    the XML element name
     * @param label   the label shown to people
     * @param options the allowed values, in display order
     * @return the field definition
     */
    public static FieldDefinition choice(String name, String label, String... options) {
        return new FieldDefinition(name, label, FieldType.CHOICE, List.of(options), true);
    }

    /**
     * A copy of this field that accepts empty values.
     *
     * @return the optional copy
     */
    public FieldDefinition optional() {
        return new FieldDefinition(name, label, type, options, false);
    }

    /**
     * Checks a value against this field's rules.
     *
     * @param value the value to check; {@code null} counts as empty
     * @return an error message people can read, or empty when the value is acceptable
     */
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
