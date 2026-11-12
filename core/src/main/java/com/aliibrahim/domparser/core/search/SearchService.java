package com.aliibrahim.domparser.core.search;

import com.aliibrahim.domparser.core.document.RecordNode;
import com.aliibrahim.domparser.core.schema.FieldDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Case-insensitive "contains" search across every field of every record. */
public final class SearchService {

    /** Creates the service; it holds no state. */
    public SearchService() {
    }

    /**
     * Finds every field value that contains {@code query}, ignoring case.
     *
     * @param records the records to search
     * @param query   the text to look for; blank finds nothing
     * @return a cursor over the matches, positioned before the first one
     */
    public SearchCursor search(List<RecordNode> records, String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<SearchMatch> matches = new ArrayList<>();
        if (!needle.isEmpty()) {
            for (RecordNode record : records) {
                for (FieldDefinition field : record.schema().fields()) {
                    String value = record.get(field.name());
                    if (value.toLowerCase(Locale.ROOT).contains(needle)) {
                        matches.add(new SearchMatch(record, field.name(), value));
                    }
                }
            }
        }
        return new SearchCursor(needle, matches);
    }
}
