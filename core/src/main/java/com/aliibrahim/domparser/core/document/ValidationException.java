package com.aliibrahim.domparser.core.document;

import java.util.Map;

/** Thrown when values do not satisfy the schema. {@link #errors()} maps field name to message. */
public final class ValidationException extends RuntimeException {

    private final transient Map<String, String> errors;

    /**
     * Creates the exception; its message joins all error messages, one per line.
     *
     * @param errors field name to error message; copied
     */
    public ValidationException(Map<String, String> errors) {
        super(String.join("\n", errors.values()));
        this.errors = Map.copyOf(errors);
    }

    /**
     * The validation errors.
     *
     * @return an unmodifiable map of field name to error message
     */
    public Map<String, String> errors() {
        return errors;
    }
}
