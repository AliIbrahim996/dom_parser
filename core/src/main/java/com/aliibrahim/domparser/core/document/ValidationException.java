package com.aliibrahim.domparser.core.document;

import java.util.Map;

/** Thrown when values do not satisfy the schema. {@link #errors()} maps field name to message. */
public final class ValidationException extends RuntimeException {

    private final transient Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super(String.join("\n", errors.values()));
        this.errors = Map.copyOf(errors);
    }

    public Map<String, String> errors() {
        return errors;
    }
}
