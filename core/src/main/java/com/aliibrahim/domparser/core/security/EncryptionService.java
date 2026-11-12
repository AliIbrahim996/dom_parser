package com.aliibrahim.domparser.core.security;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Encrypts parts of a document following the W3C XML Encryption standard. Implementations return a new
 * {@link Document}; callers hand it to {@code XmlDocumentModel.replaceDocument}.
 */
public interface EncryptionService {

    /**
     * Encrypts the whole document: the root element is replaced by an {@code EncryptedData} element.
     *
     * @param document the document to encrypt; not modified
     * @return a new, encrypted document
     */
    Document encryptDocument(Document document);

    /**
     * Encrypts one element: {@code target} is replaced by an {@code EncryptedData} element.
     *
     * @param document the document that contains {@code target}; not modified
     * @param target   the element to encrypt
     * @return a new document with the element encrypted
     */
    Document encryptElement(Document document, Element target);

    /**
     * Decrypts every {@code EncryptedData} element that this service's key can open.
     *
     * @param document the document to decrypt; not modified
     * @return a new document without encrypted content
     */
    Document decryptAll(Document document);

    /**
     * Counts the encrypted parts of a document.
     *
     * @param document the document to inspect
     * @return the number of {@code EncryptedData} elements
     */
    int countEncrypted(Document document);
}
