package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for flashcard operations.
 *
 * <p>Contains all SQL queries used by FlashcardJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class FlashcardSqlQueries {

    private FlashcardSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to select flashcard by ID.
     */
    public static final String SELECT_FLASHCARD_BY_ID =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            WHERE f.id = ?
            """;

    /**
     * SQL query to select flashcards by deck ID.
     * Ordered by updated_at DESC to show newest cards first.
     */
    public static final String SELECT_FLASHCARDS_BY_DECK_ID =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            WHERE f.deck_id = ?
            ORDER BY f.created_at DESC
            """;

    /**
     * Base SQL query for flashcards with dynamic WHERE conditions.
     * Use with QueryBuilder for flexible filtering.
     */
    public static final String SELECT_FLASHCARDS_BASE =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            """;

    /**
     * Base SQL query for counting flashcards with dynamic WHERE conditions.
     * Use with QueryBuilder for flexible filtering.
     */
    public static final String COUNT_FLASHCARDS_BASE =
            """
            SELECT COUNT(1)
            FROM flashcards f
            """;

    /**
     * SQL query to insert new flashcard and return generated ID.
     */
    public static final String INSERT_FLASHCARD_RETURNING_ID =
            """
            INSERT INTO flashcards (deck_id, front_text, back_text, example, image_url, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

    /**
     * SQL query to update existing flashcard.
     */
    public static final String UPDATE_FLASHCARD =
            """
            UPDATE flashcards
            SET deck_id = ?, front_text = ?, back_text = ?, example = ?, image_url = ?, updated_at = ?
            WHERE id = ?
            """;

    /**
     * SQL query to delete flashcard by ID.
     */
    public static final String DELETE_FLASHCARD =
            """
            DELETE FROM flashcards
            WHERE id = ?
            """;

    /**
     * SQL query to delete all flashcards by deck ID.
     */
    public static final String DELETE_FLASHCARDS_BY_DECK_ID =
            """
            DELETE FROM flashcards
            WHERE deck_id = ?
            """;

    /**
     * SQL query to count flashcards by deck ID.
     */
    public static final String COUNT_FLASHCARDS_BY_DECK_ID =
            """
            SELECT COUNT(1)
            FROM flashcards
            WHERE deck_id = ?
            """;

    /**
     * SQL query to count flashcards for multiple decks in single query.
     * Returns only decks that have flashcards (decks with 0 flashcards are excluded).
     * Note: Requires dynamic SQL for IN clause (deckIds parameter).
     */
    public static final String COUNT_FLASHCARDS_BY_DECK_IDS_TEMPLATE =
            """
            SELECT deck_id, COUNT(*) as count
            FROM flashcards
            WHERE deck_id IN (%s)
            GROUP BY deck_id
            """;
}
