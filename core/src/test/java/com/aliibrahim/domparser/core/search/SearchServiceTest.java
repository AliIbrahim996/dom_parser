package com.aliibrahim.domparser.core.search;

import com.aliibrahim.domparser.core.document.XmlDocumentModel;
import com.aliibrahim.domparser.core.document.XmlIO;
import com.aliibrahim.domparser.core.schema.UserSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {

    @Test
    void findsAcrossFieldsCaseInsensitivelyAndWraps() throws Exception {
        XmlDocumentModel model = XmlDocumentModel.of(XmlIO.parse("""
                <Users>
                  <User id="1"><firstName>Ali</firstName><lastName>Ibrahim</lastName><age>25</age><gender>Male</gender></User>
                  <User id="2"><firstName>Leen</firstName><lastName>Mualla</lastName><age>25</age><gender>Female</gender></User>
                  <User id="3"><firstName>Sam</firstName><lastName>Alison</lastName><age>40</age><gender>Male</gender></User>
                </Users>
                """), new UserSchema());

        SearchCursor cursor = new SearchService().search(model.records(), "ali");

        assertEquals(2, cursor.size());
        assertEquals("1", cursor.next().orElseThrow().record().id());
        assertEquals("lastName", cursor.next().orElseThrow().field());
        assertEquals(2, cursor.position());
        assertEquals("1", cursor.next().orElseThrow().record().id(), "wraps to first");
        assertEquals("3", cursor.previous().orElseThrow().record().id(), "wraps to last");
    }

    @Test
    void blankQueryMatchesNothing() {
        SearchCursor cursor = new SearchService().search(java.util.List.of(), "  ");
        assertTrue(cursor.isEmpty());
        assertTrue(cursor.next().isEmpty());
    }
}
