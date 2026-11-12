package com.aliibrahim.domparser.core.document;

/** Notified after every change to an {@link XmlDocumentModel}. */
@FunctionalInterface
public interface DocumentListener {
    void documentChanged(XmlDocumentModel model, DocumentChange change);
}
