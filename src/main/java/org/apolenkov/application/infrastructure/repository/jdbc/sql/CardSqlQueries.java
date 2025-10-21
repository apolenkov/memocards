package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for card operations.
 *
 * <p>Contains all SQL queries used by CardJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class CardSqlQueries {

    private CardSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to select card by ID.
     */
    public static final String SELECT_CARD_BY_ID =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM cards f
            WHERE f.id = ?
            """;

    /**
     * SQL query to select cards by deck ID.
     * Ordered by updated_at DESC to show newest cards first.
     */
    public static final String SELECT_CARDS_BY_DECK_ID =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM cards f
            WHERE f.deck_id = ?
            ORDER BY f.created_at DESC
            """;

    /**
     * Base SQL query for cards with dynamic WHERE conditions.
     * Use with QueryBuilder for flexible filtering.
     */
    public static final String SELECT_CARDS_BASE =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM cards f
            """;

    /**
     * Base SQL query for counting cards with dynamic WHERE conditions.
     * Use with QueryBuilder for flexible filtering.
     */
    public static final String COUNT_CARDS_BASE =
            """
            SELECT COUNT(1)
            FROM cards f
            """;

    /**
     * SQL query to insert new card and return generated ID.
     */
    public static final String INSERT_CARD_RETURNING_ID =
            """
            INSERT INTO cards (deck_id, front_text, back_text, example, image_url, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

    /**
     * SQL query to update existing card.
     */
    public static final String UPDATE_CARD =
            """
            UPDATE cards
            SET deck_id = ?, front_text = ?, back_text = ?, example = ?, image_url = ?, updated_at = ?
            WHERE id = ?
            """;

    /**
     * SQL query to delete card by ID.
     */
    public static final String DELETE_CARD = """
            DELETE FROM cards
            WHERE id = ?
            """;

    /**
     * SQL query to delete all cards by deck ID.
     */
    public static final String DELETE_CARDS_BY_DECK_ID =
            """
            DELETE FROM cards
            WHERE deck_id = ?
            """;

    /**
     * SQL query to count cards by deck ID.
     */
    public static final String COUNT_CARDS_BY_DECK_ID =
            """
            SELECT COUNT(1)
            FROM cards
            WHERE deck_id = ?
            """;

    /**
     * SQL query to count cards for multiple decks in single query.
     * Returns only decks that have cards (decks with 0 cards are excluded).
     * Note: Requires dynamic SQL for IN clause (deckIds parameter).
     */
    public static final String COUNT_CARDS_BY_DECK_IDS_TEMPLATE =
            """
            SELECT deck_id, COUNT(*) as count
            FROM cards
            WHERE deck_id IN (%s)
            GROUP BY deck_id
            """;
}
