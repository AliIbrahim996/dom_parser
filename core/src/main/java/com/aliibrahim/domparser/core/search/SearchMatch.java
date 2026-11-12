package com.aliibrahim.domparser.core.search;

import com.aliibrahim.domparser.core.document.RecordNode;

/** A record field whose value matched a query. */
public record SearchMatch(RecordNode record, String field, String value) {
}
