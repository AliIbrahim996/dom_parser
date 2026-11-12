package com.aliibrahim.domparser.core.search;

import com.aliibrahim.domparser.core.document.RecordNode;

/**
 * A record field whose value matched a query.
 *
 * @param record the record that matched
 * @param field  the name of the matching field
 * @param value  the field's full value
 */
public record SearchMatch(RecordNode record, String field, String value) {
}
