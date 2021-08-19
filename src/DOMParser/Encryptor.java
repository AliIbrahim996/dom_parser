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
    /**
     * @param d the document
     * @param e the element
     * @return the document after encrypting a node
     * */
    public static Document encrypt_element(Document d, org.w3c.dom.Element e){
        org.apache.xml.security.Init.init();
        Key aesSymmetricKey = null;
        Key deSedeEncryptKey = null;
        EncryptedKey encryptedKey = null;
        try {
            aesSymmetricKey = generateDataEncryptionKey();
            deSedeEncryptKey = generateKeyEncryptionKey();
            String algorithmURI = XMLCipher.TRIPLEDES_KeyWrap;
            XMLCipher keyCipher = XMLCipher.getInstance(algorithmURI);
            keyCipher.init(XMLCipher.WRAP_MODE, deSedeEncryptKey);
            encryptedKey = keyCipher.encryptKey(d, aesSymmetricKey);
            return encrypt_element(d, aesSymmetricKey, encryptedKey, e);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return d;
    }

    /**
     * Encrypt xml node
     *
     * @param d               the document
     * @param aesSymmetricKey the encrypt symmetric key
     * @param encryptedKey    the encrypted key
     * @param e               the element
     * @return the document after encrypting a node
     * @throws Exception the exception
     */
    private static Document encrypt_element(Document d, Key aesSymmetricKey, EncryptedKey encryptedKey, org.w3c.dom.Element e) throws Exception {
        String algorithmURI = XMLCipher.AES_128;
        XMLCipher xmlCipher = XMLCipher.getInstance(algorithmURI);
        xmlCipher.init(XMLCipher.ENCRYPT_MODE, aesSymmetricKey);
        EncryptedData encryptedData = xmlCipher.getEncryptedData();
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
