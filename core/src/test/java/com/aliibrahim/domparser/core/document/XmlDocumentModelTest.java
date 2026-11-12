package com.aliibrahim.domparser.core.document;

import com.aliibrahim.domparser.core.schema.SchemaRegistry;
import com.aliibrahim.domparser.core.schema.UserSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class XmlDocumentModelTest {

    static final String XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <Users>
              <User id="1"><firstName>Ali</firstName><lastName>Ibrahim</lastName><age>25</age><gender>Male</gender></User>
              <User id="2"><firstName>Leen</firstName><lastName>Mualla</lastName><age>25</age><gender>Female</gender></User>
            </Users>
            """;

    @TempDir
    Path dir;

    XmlDocumentModel open() throws Exception {
        Path file = dir.resolve("users.xml");
        Files.writeString(file, XML);
        return XmlDocumentModel.open(file, SchemaRegistry.discover());
    }

    @Test
    void detectsSchemaAndReadsRecords() throws Exception {
        XmlDocumentModel model = open();
        assertEquals("users", model.schema().orElseThrow().id());
        assertEquals(2, model.records().size());
        assertEquals("Leen", model.records().get(1).get("firstName"));
        assertFalse(model.isDirty());
    }

    @Test
    void addAssignsNextIdAndNotifies() throws Exception {
        XmlDocumentModel model = open();
        List<DocumentChange> events = new ArrayList<>();
        model.addListener((m, c) -> events.add(c));

        RecordNode added = model.addRecord(Map.of("firstName", "Sam", "lastName", "Lee", "age", "30", "gender", "Male"));

        assertEquals("3", added.id());
        assertEquals(3, model.records().size());
        assertEquals(List.of(DocumentChange.STRUCTURE), events);
        assertTrue(model.isDirty());
    }

    @Test
    void rejectsInvalidValues() throws Exception {
        XmlDocumentModel model = open();
        ValidationException ex = assertThrows(ValidationException.class, () ->
                model.addRecord(Map.of("firstName", "", "lastName", "X", "age", "old", "gender", "Other")));
        assertEquals(3, ex.errors().size());
        assertTrue(ex.errors().containsKey("age"));
        assertEquals(2, model.records().size());
    }

    @Test
    void inTreeEditIsValidatedForSchemaFields() throws Exception {
        XmlDocumentModel model = open();
        var ageElement = model.records().get(0).element().getChild("age");
        assertThrows(ValidationException.class, () -> model.setElementText(ageElement, "abc"));
        model.setElementText(ageElement, "26");
        assertEquals("26", model.records().get(0).get("age"));
    }

    @Test
    void updateDeleteAndSaveRoundTrip() throws Exception {
        XmlDocumentModel model = open();
        RecordNode first = model.records().get(0);
        Map<String, String> values = first.values();
        values.put("lastName", "Ali");
        model.updateRecord(first, values);
        model.deleteRecord(model.records().get(1));

        Path out = dir.resolve("out.xml");
        model.saveAs(out);
        assertFalse(model.isDirty());

        XmlDocumentModel reloaded = XmlDocumentModel.open(out, SchemaRegistry.discover());
        assertEquals(1, reloaded.records().size());
        assertEquals("Ali", reloaded.records().get(0).get("lastName"));
    }

    @Test
    void unknownDocumentsOpenWithoutSchema() throws Exception {
        Path file = dir.resolve("other.xml");
        Files.writeString(file, "<catalog><book/></catalog>");
        XmlDocumentModel model = XmlDocumentModel.open(file, SchemaRegistry.discover());
        assertTrue(model.schema().isEmpty());
        assertTrue(model.records().isEmpty());
        assertThrows(IllegalStateException.class, () -> model.addRecord(Map.of()));
    }

    @Test
    void refusesDoctypes() throws Exception {
        Path file = dir.resolve("xxe.xml");
        Files.writeString(file, "<!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><Users>&e;</Users>");
        assertThrows(Exception.class, () -> XmlDocumentModel.open(file, SchemaRegistry.discover()));
    }

    @Test
    void createStartsEmptyAndDirty() {
        XmlDocumentModel model = XmlDocumentModel.create(new UserSchema());
        assertTrue(model.isDirty());
        assertEquals("Users", model.document().getRootElement().getName());
        assertEquals("1", model.addRecord(Map.of("firstName", "A", "lastName", "B", "age", "1", "gender", "female")).id());
    }

    @Test
    void failedWriteLeavesNoTempFile(@TempDir Path dir) throws Exception {
        Path target = dir.resolve("blocked.xml");
        Files.createDirectories(target.resolve("child")); // a non-empty directory cannot be replaced
        assertThrows(java.io.IOException.class, () -> XmlIO.write(XmlIO.parse(XML), target));
        assertFalse(Files.exists(dir.resolve("blocked.xml.tmp")));
    }
}
