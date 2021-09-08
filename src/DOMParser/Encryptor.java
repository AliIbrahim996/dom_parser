package DOMParser;

import org.apache.xml.security.encryption.EncryptedData;
import org.apache.xml.security.encryption.EncryptedKey;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Document;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;

public class Encryptor {

    private static Key aesSymmetricKey = null;
    private static Key deSedeEncryptKey = null;
    private static EncryptedKey encryptedKey = null;
    private static XMLCipher xmlCipher;

    private static XMLCipher initializer() throws Exception {
        aesSymmetricKey = generateDataEncryptionKey();
        deSedeEncryptKey = generateKeyEncryptionKey();
        String algorithmURI = XMLCipher.TRIPLEDES_KeyWrap;
        XMLCipher keyCipher = XMLCipher.getInstance(algorithmURI);
        keyCipher.init(XMLCipher.WRAP_MODE, deSedeEncryptKey);
        return keyCipher;
    }

    /**
     * @param d the document
     * @param e the element
     * @return the document after encrypting a node
     */
    public static Document encrypt_node(Document d, org.w3c.dom.Element e) {
        org.apache.xml.security.Init.init();
        try {
            XMLCipher keyCipher = initializer();
            encryptedKey = keyCipher.encryptKey(d, aesSymmetricKey);
            return encrypt_(d, encryptedKey, e);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return d;
    }

    public static Document encrypt_document(Document d) {
        org.apache.xml.security.Init.init();
        try {
            XMLCipher keyCipher = initializer();
            encryptedKey = keyCipher.encryptKey(d, aesSymmetricKey);
            return encrypt_(d, aesSymmetricKey, encryptedKey);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return d;
    }

    private static Document encrypt_(Document d, Key aesSymmetricKey, EncryptedKey encryptedKey) throws Exception {
        String algorithmURI = XMLCipher.AES_128;
        XMLCipher xmlCipher = XMLCipher.getInstance(algorithmURI);
        xmlCipher.init(XMLCipher.ENCRYPT_MODE, aesSymmetricKey);
        EncryptedData encryptedData = xmlCipher.getEncryptedData();
        encryptedData.setMimeType("text/xml");
        KeyInfo keyInfo = new KeyInfo(d);
        keyInfo.add(encryptedKey);
        encryptedData.setKeyInfo(keyInfo);
        xmlCipher.doFinal(d, d);
        return d;
    }

    /**
     * Encrypt xml node
     *
     * @param d            the document
     * @param encryptedKey the encrypted key
     * @param e            the element
     * @return the document after encrypting a node
     * @throws Exception the exception
     */
    private static Document encrypt_(Document d, EncryptedKey encryptedKey, org.w3c.dom.Element e) throws Exception {
        String algorithmURI = XMLCipher.AES_128;
        xmlCipher = XMLCipher.getInstance(algorithmURI);
        xmlCipher.init(XMLCipher.ENCRYPT_MODE, aesSymmetricKey);
        EncryptedData encryptedData = xmlCipher.getEncryptedData();
        System.out.println("Encrypted data type: " + encryptedData.getType());
        /*
         * Setting keyinfo inside the encrypted data being prepared.
         */
        KeyInfo keyInfo = new KeyInfo(d);
        keyInfo.add(encryptedKey);
        encryptedData.setKeyInfo(keyInfo);
        xmlCipher.doFinal(d, e, true);
        return d;
    }

    /**
     * Generate key encryption key.
     *
     * @return the secret key
     * @throws Exception the exception
     */
    private static SecretKey generateKeyEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
        SecretKey kek = keyGenerator.generateKey();
        return kek;
    }

    /**
     * Generate data encryption key.
     *
     * @return the secret key
     * @throws Exception the exception
     */
    private static SecretKey generateDataEncryptionKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }
}
