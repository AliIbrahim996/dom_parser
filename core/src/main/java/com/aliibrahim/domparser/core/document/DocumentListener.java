package com.aliibrahim.domparser.core.document;

/** Notified after every change to an {@link XmlDocumentModel}. */
@FunctionalInterface
public interface DocumentListener {
    /**
     * Called after {@code model} has changed.
     *
     * @param model  the document that changed
     * @param change what kind of change happened
     */
    void documentChanged(XmlDocumentModel model, DocumentChange change);
}
