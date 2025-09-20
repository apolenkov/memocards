package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for deck operations.
 *
 * <p>Contains all SQL queries used by DeckJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class DeckSqlQueries {

    private DeckSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to select all decks.
     */
    public static final String SELECT_ALL_DECKS =
            """
            SELECT d.id, d.user_id, d.title, d.description, d.created_at, d.updated_at
            FROM decks d
            ORDER BY d.created_at DESC
            """;

    /**
     * SQL query to select deck by ID.
     */
    public static final String SELECT_DECK_BY_ID =
            """
            SELECT d.id, d.user_id, d.title, d.description, d.created_at, d.updated_at
            FROM decks d
            WHERE d.id = ?
            """;

    /**
     * SQL query to select decks by user ID.
     */
    public static final String SELECT_DECKS_BY_USER_ID =
            """
            SELECT d.id, d.user_id, d.title, d.description, d.created_at, d.updated_at
            FROM decks d
            WHERE d.user_id = ?
            ORDER BY d.created_at DESC
            """;

    /**
     * SQL query to insert new deck and return generated ID.
     */
    public static final String INSERT_DECK_RETURNING_ID =
            """
            INSERT INTO decks (user_id, title, description, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """;

    /**
     * SQL query to update existing deck.
     */
    public static final String UPDATE_DECK =
            """
            UPDATE decks
            SET user_id = ?, title = ?, description = ?, updated_at = ?
            WHERE id = ?
            """;

    /**
     * SQL query to delete deck by ID.
     */
    public static final String DELETE_DECK = """
            DELETE FROM decks
            WHERE id = ?
            """;
}
