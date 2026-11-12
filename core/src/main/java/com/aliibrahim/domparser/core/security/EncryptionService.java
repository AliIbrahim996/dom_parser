package com.aliibrahim.domparser.core.security;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Encrypts parts of a document following the W3C XML Encryption standard. Implementations return a new
 * {@link Document}; callers hand it to {@code XmlDocumentModel.replaceDocument}.
 */
public interface EncryptionService {

    /** Replaces the root element with an {@code EncryptedData} element. */
    Document encryptDocument(Document document);

    /** Replaces {@code target} (an element of {@code document}) with an {@code EncryptedData} element. */
    Document encryptElement(Document document, Element target);

    /** Decrypts every {@code EncryptedData} element that was produced with this service's key. */
    Document decryptAll(Document document);

    /** Number of {@code EncryptedData} elements in the document. */
    int countEncrypted(Document document);
}
