package com.aliibrahim.domparser.core.schema;

/** The kind of value a {@link FieldDefinition} holds. Drives validation and the editor widget used by clients. */
public enum FieldType {
    /** Free text. */
    TEXT,
    /** A whole number. */
    INTEGER,
    /** One of a fixed list of options. */
    CHOICE
}
