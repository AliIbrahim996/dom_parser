package com.aliibrahim.domparser.core.security;

import com.aliibrahim.domparser.core.document.XmlIO;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XmlEncryptionServiceTest {

    static final String XML = """
            <Users>
              <User id="1"><firstName>Ali</firstName><lastName>Ibrahim</lastName><age>25</age><gender>Male</gender></User>
              <User id="2"><firstName>Leen</firstName><lastName>Mualla</lastName><age>25</age><gender>Female</gender></User>
            </Users>
            """;

    final XmlEncryptionService service = new XmlEncryptionService();

    @Test
    void encryptsSingleFieldAndDecryptsItBack() throws Exception {
        Document doc = XmlIO.parse(XML);
        Element lastName = doc.getRootElement().getChildren("User").get(1).getChild("lastName");

        Document encrypted = service.encryptElement(doc, lastName);
        String xml = XmlIO.toString(encrypted);

        assertFalse(xml.contains("Mualla"));
        assertTrue(xml.contains("Ibrahim"), "other content untouched");
        assertEquals(1, service.countEncrypted(encrypted));

        Document decrypted = service.decryptAll(encrypted);
        assertEquals(0, service.countEncrypted(decrypted));
        assertEquals("Mualla", decrypted.getRootElement().getChildren("User").get(1).getChildText("lastName"));
    }

    @Test
    void encryptsRecordAndWholeDocument() throws Exception {
        Document doc = XmlIO.parse(XML);
        Document recordEncrypted = service.encryptElement(doc, doc.getRootElement().getChildren("User").get(0));
        assertEquals(1, recordEncrypted.getRootElement().getChildren("User").size());

        Document all = service.encryptDocument(recordEncrypted);
        assertTrue(XmlEncryptionService.isEncryptedData(all.getRootElement()));
        assertEquals(1, service.countEncrypted(all), "an encrypted root is counted once");
        assertFalse(XmlIO.toString(all).contains("Leen"));

        Document restored = service.decryptAll(all);
        assertEquals(2, restored.getRootElement().getChildren("User").size());
        assertEquals("Ali", restored.getRootElement().getChildren("User").get(0).getChildText("firstName"));
    }

    @Test
    void anotherSessionCannotDecrypt() throws Exception {
        Document doc = XmlIO.parse(XML);
        Document encrypted = service.encryptDocument(doc);
        assertThrows(EncryptionException.class, () -> new XmlEncryptionService().decryptAll(encrypted));
    }

    @Test
    void nextIdSkipsIdsHiddenByEncryption() throws Exception {
        var model = com.aliibrahim.domparser.core.document.XmlDocumentModel.of(
                XmlIO.parse(XML), new com.aliibrahim.domparser.core.schema.UserSchema());
        Element user2 = model.records().get(1).element();
        model.replaceDocument(service.encryptElement(model.document(), user2));

        var added = model.addRecord(java.util.Map.of(
                "firstName", "Sara", "lastName", "Haddad", "age", "31", "gender", "Female"));
        assertEquals("3", added.id());
    }
}
