package com.aliibrahim.domparser.core.security;

import com.aliibrahim.domparser.core.document.DocumentConverter;
import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.utils.EncryptionConstants;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

/**
 * Apache Santuario implementation. Each operation uses a fresh AES-256-GCM data key, wrapped with an AES-256 key
 * encryption key (KEK) and embedded in the {@code KeyInfo}. The KEK lives as long as this object (one app session),
 * so content encrypted in a session can be decrypted again in that session.
 */
public final class XmlEncryptionService implements EncryptionService {

    private static final Namespace XENC = Namespace.getNamespace(EncryptionConstants.EncryptionSpecNS);

    static {
        org.apache.xml.security.Init.init();
    }

    private final SecretKey keyEncryptionKey;

    /** Creates a service with a new random AES-256 key encryption key for this session. */
    public XmlEncryptionService() {
        this(generateAesKey());
    }

    /**
     * Creates a service that wraps data keys with the given key.
     *
     * @param keyEncryptionKey an AES-256 key used with AES key wrap
     */
    public XmlEncryptionService(SecretKey keyEncryptionKey) {
        this.keyEncryptionKey = keyEncryptionKey;
    }

    /**
     * {@inheritDoc}
     *
     * @throws EncryptionException if encryption fails
     */
    @Override
    public Document encryptDocument(Document document) {
        return encryptElement(document, document.getRootElement());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Uses a fresh AES-256-GCM data key, wrapped with this service's key and stored in the {@code KeyInfo}.
     *
     * @throws IllegalArgumentException if {@code target} is not part of {@code document} or is already encrypted
     * @throws EncryptionException      if encryption fails
     */
    @Override
    public Document encryptElement(Document document, Element target) {
        if (target.getDocument() != document) {
            throw new IllegalArgumentException("Target element does not belong to the document");
        }
        if (isEncryptedData(target)) {
            throw new IllegalArgumentException("Element is already encrypted");
        }
        org.w3c.dom.Document dom = DocumentConverter.toDom(document);
        org.w3c.dom.Element domTarget = DocumentConverter.locate(dom, target);
        try {
            SecretKey dataKey = generateAesKey();

            XMLCipher keyCipher = XMLCipher.getInstance(XMLCipher.AES_256_KeyWrap);
            keyCipher.init(XMLCipher.WRAP_MODE, keyEncryptionKey);
            EncryptedKey encryptedKey = keyCipher.encryptKey(dom, dataKey);

            XMLCipher cipher = XMLCipher.getInstance(XMLCipher.AES_256_GCM);
            cipher.init(XMLCipher.ENCRYPT_MODE, dataKey);
            EncryptedData encryptedData = cipher.getEncryptedData();
            KeyInfo keyInfo = new KeyInfo(dom);
            keyInfo.add(encryptedKey);
            encryptedData.setKeyInfo(keyInfo);
            cipher.doFinal(dom, domTarget, false);
        } catch (Exception e) {
            throw new EncryptionException("Encryption failed: " + e.getMessage(), e);
        }
        return DocumentConverter.toJdom(dom);
    }

    /**
     * {@inheritDoc}
     *
     * @throws EncryptionException if any element was encrypted with a different key (another session)
     */
    @Override
    public Document decryptAll(Document document) {
        org.w3c.dom.Document dom = DocumentConverter.toDom(document);
        try {
            org.w3c.dom.Element encrypted;
            while ((encrypted = firstEncryptedData(dom)) != null) {
                XMLCipher cipher = XMLCipher.getInstance();
                cipher.init(XMLCipher.DECRYPT_MODE, null);
                cipher.setKEK(keyEncryptionKey);
                cipher.doFinal(dom, encrypted);
            }
        } catch (Exception e) {
            throw new EncryptionException(
                    "Decryption failed. Content can only be decrypted in the session that encrypted it.", e);
        }
        return DocumentConverter.toJdom(dom);
    }

    /** {@inheritDoc} */
    @Override
    public int countEncrypted(Document document) {
        // Document.getDescendants() already includes the root element.
        int count = 0;
        for (Element ignored : document.getDescendants(Filters.element(EncryptionConstants._TAG_ENCRYPTEDDATA, XENC))) {
            count++;
        }
        return count;
    }

    /**
     * Whether {@code element} is an {@code EncryptedData} element or sits inside one.
     *
     * @param element the element to test, or {@code null}
     * @return true when the element or one of its ancestors is encrypted data
     */
    public static boolean isWithinEncryptedData(Element element) {
        for (Element e = element; e != null; e = e.getParentElement()) {
            if (isEncryptedData(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether {@code element} is an XML Encryption {@code EncryptedData} element.
     *
     * @param element the element to test
     * @return true for {@code xenc:EncryptedData}
     */
    public static boolean isEncryptedData(Element element) {
        return EncryptionConstants._TAG_ENCRYPTEDDATA.equals(element.getName())
                && XENC.getURI().equals(element.getNamespaceURI());
    }

    /** The first {@code EncryptedData} element in document order, or {@code null} when none is left. */
    private static org.w3c.dom.Element firstEncryptedData(org.w3c.dom.Document dom) {
        org.w3c.dom.NodeList list = dom.getElementsByTagNameNS(
                EncryptionConstants.EncryptionSpecNS, EncryptionConstants._TAG_ENCRYPTEDDATA);
        return list.getLength() == 0 ? null : (org.w3c.dom.Element) list.item(0);
    }

    /** Generates a random AES-256 key. */
    private static SecretKey generateAesKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            return generator.generateKey();
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("AES is not available", e);
        }
    }
}
