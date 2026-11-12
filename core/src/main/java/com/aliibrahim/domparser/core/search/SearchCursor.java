package com.aliibrahim.domparser.core.search;

import java.util.List;
import java.util.Optional;

/** Walks over a fixed list of matches with wrap-around next/previous. */
public final class SearchCursor {

    private final String query;
    private final List<SearchMatch> matches;
    private int index = -1;

    SearchCursor(String query, List<SearchMatch> matches) {
        this.query = query;
        this.matches = List.copyOf(matches);
    }

    public String query() {
        return query;
    }

    public List<SearchMatch> matches() {
        return matches;
    }

    public int size() {
        return matches.size();
    }

    public boolean isEmpty() {
        return matches.isEmpty();
    }

    /** 1-based position of the current match; 0 before the first call to {@link #next()}. */
    public int position() {
        return index + 1;
    }

    /** Moves to the 1-based {@code position} (clamped to the available matches) without returning it. */
    public void seek(int position) {
        index = Math.min(Math.max(position, 0), matches.size()) - 1;
    }

    public Optional<SearchMatch> current() {
        return index < 0 || matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(index));
    }

    public Optional<SearchMatch> next() {
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        index = (index + 1) % matches.size();
        return current();
    }

    public Optional<SearchMatch> previous() {
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        index = index <= 0 ? matches.size() - 1 : index - 1;
        return current();
    }
}
