package org.apolenkov.application.infrastructure.repository.jdbc.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for dynamic SQL queries for flashcard filtering.
 * Supports combinations of search query and known/unknown status filtering.
 */
public final class FlashcardQueryBuilder {

    private static final String KC_ID_IS_NULL = "kc.id IS NULL";

    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private boolean hasKnownJoin = false;

    /**
     * Adds deck ID condition.
     *
     * @param deckId deck identifier
     * @return this builder for method chaining
     */
    public FlashcardQueryBuilder withDeckId(final long deckId) {
        conditions.add("f.deck_id = ?");
        parameters.add(deckId);
        return this;
    }

    /**
     * Adds search query condition (case-insensitive search in front_text, back_text, example).
     *
     * @param searchQuery search query (can be null or empty)
     */
    public void withSearchQuery(final String searchQuery) {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String searchPattern = "%" + searchQuery.trim() + "%";
            conditions.add("(f.front_text ILIKE ? OR f.back_text ILIKE ? OR f.example ILIKE ?)");
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }
    }

    /**
     * Adds known status filter (INNER JOIN with known_cards).
     */
    public void withKnownStatus() {
        hasKnownJoin = true;
    }

    /**
     * Adds unknown status filter (LEFT JOIN with known_cards + IS NULL).
     */
    public void withUnknownStatus() {
        hasKnownJoin = true;
        conditions.add(KC_ID_IS_NULL);
    }

    /**
     * Builds the complete SQL query for selecting flashcards.
     *
     * @param baseQuery base SELECT query (from FlashcardSqlQueries)
     * @return complete SQL query with WHERE and ORDER BY clauses
     */
    public String buildSelectQuery(final String baseQuery) {
        StringBuilder sql = new StringBuilder(baseQuery);

        // Add JOIN if needed for known/unknown status
        if (hasKnownJoin) {
            if (conditions.contains(KC_ID_IS_NULL)) {
                // Unknown status - LEFT JOIN
                sql.append(" LEFT JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id");
            } else {
                // Known status - INNER JOIN
                sql.append(" INNER JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id");
            }
        }

        // Add WHERE clause
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // Add ORDER BY (newest first for better UX)
        sql.append(" ORDER BY f.created_at DESC");

        return sql.toString();
    }

    /**
     * Builds the complete SQL query for counting flashcards.
     *
     * @param baseQuery base COUNT query (from FlashcardSqlQueries)
     * @return complete SQL query with WHERE clause
     */
    public String buildCountQuery(final String baseQuery) {
        StringBuilder sql = new StringBuilder(baseQuery);

        // Add JOIN if needed for known/unknown status
        if (hasKnownJoin) {
            if (conditions.contains(KC_ID_IS_NULL)) {
                // Unknown status - LEFT JOIN
                sql.append(" LEFT JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id");
            } else {
                // Known status - INNER JOIN
                sql.append(" INNER JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id");
            }
        }

        // Add WHERE clause
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }

    /**
     * Gets the list of parameters for the prepared statement.
     *
     * @return array of parameters in the order they appear in the query
     */
    public Object[] getParameters() {
        return parameters.toArray();
    }

    /**
     * Gets the list of parameters for pagination (adds limit and offset).
     *
     * @param limit maximum number of results
     * @param offset offset for pagination
     * @return array of parameters including pagination
     */
    public Object[] getParametersWithPagination(final int limit, final long offset) {
        List<Object> params = new ArrayList<>(parameters);
        params.add(limit);
        params.add(offset);
        return params.toArray();
    }

    /**
     * Gets the paginated SQL query (adds LIMIT and OFFSET).
     *
     * @param baseQuery base SELECT query
     * @return complete SQL query with pagination placeholders
     */
    public String buildSelectQueryWithPagination(final String baseQuery) {
        return buildSelectQuery(baseQuery) + " LIMIT ? OFFSET ?";
    }
}
