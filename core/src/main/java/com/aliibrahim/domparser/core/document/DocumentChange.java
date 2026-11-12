package com.aliibrahim.domparser.core.document;

/** What happened to an {@link XmlDocumentModel}. */
public enum DocumentChange {
    /** The whole JDOM tree was replaced (encryption, ...). Element references are no longer valid. */
    REPLACED,
    /** Records were added or removed. */
    STRUCTURE,
    /** Text/attribute values changed in place. */
    CONTENT,
    /** The document was written to disk; dirty flag cleared. */
    SAVED
}
