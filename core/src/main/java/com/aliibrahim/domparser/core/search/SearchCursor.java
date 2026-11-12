package com.aliibrahim.domparser.core.search;

import java.util.List;
import java.util.Optional;

/** Walks over a fixed list of matches with wrap-around next/previous. */
public final class SearchCursor {

    private final String query;
    private final List<SearchMatch> matches;
    private int index = -1;

    /** Created by {@link SearchService}; starts before the first match. */
    SearchCursor(String query, List<SearchMatch> matches) {
        this.query = query;
        this.matches = List.copyOf(matches);
    }

    /**
     * The normalised (trimmed, lower-case) query.
     *
     * @return the query text
     */
    public String query() {
        return query;
    }

    /**
     * All matches, in record and field order.
     *
     * @return an unmodifiable list
     */
    public List<SearchMatch> matches() {
        return matches;
    }

    /**
     * Number of matches.
     *
     * @return the match count
     */
    public int size() {
        return matches.size();
    }

    /**
     * Whether nothing matched.
     *
     * @return true when there are no matches
     */
    public boolean isEmpty() {
        return matches.isEmpty();
    }

    /**
     * The 1-based position of the current match.
     *
     * @return the position, or 0 before the first call to {@link #next()} or {@link #previous()}
     */
    public int position() {
        return index + 1;
    }

    /**
     * Moves to a 1-based position without returning the match. Used to keep the place after results are
     * recomputed.
     *
     * @param position the position to move to; clamped to the available matches, 0 means before the first
     */
    public void seek(int position) {
        index = Math.min(Math.max(position, 0), matches.size()) - 1;
    }

    /**
     * The match the cursor is on.
     *
     * @return the current match, or empty before the first {@link #next()} / {@link #previous()}
     */
    public Optional<SearchMatch> current() {
        return index < 0 || matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(index));
    }

    /**
     * Moves to the next match, wrapping from the last back to the first.
     *
     * @return the new current match, or empty when there are no matches
     */
    public Optional<SearchMatch> next() {
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        index = (index + 1) % matches.size();
        return current();
    }

    /**
     * Moves to the previous match, wrapping from the first to the last.
     *
     * @return the new current match, or empty when there are no matches
     */
    public Optional<SearchMatch> previous() {
        if (matches.isEmpty()) {
            return Optional.empty();
        }
        index = index <= 0 ? matches.size() - 1 : index - 1;
        return current();
    }
}
