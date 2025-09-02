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
     */
    public static final String SELECT_FLASHCARDS_BY_DECK_ID =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            WHERE f.deck_id = ?
            ORDER BY f.created_at ASC
            """;

    /**
     * SQL query to insert new flashcard.
     */
    public static final String INSERT_FLASHCARD =
            """
            INSERT INTO flashcards (deck_id, front_text, back_text, example, image_url, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
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
     * SQL query to check if flashcard exists by ID.
     */
    public static final String EXISTS_FLASHCARD_BY_ID =
            """
            SELECT COUNT(1)
            FROM flashcards
            WHERE id = ?
            """;

    /**
     * SQL query to check if flashcard belongs to deck.
     */
    public static final String EXISTS_FLASHCARD_BY_ID_AND_DECK_ID =
            """
            SELECT COUNT(1)
            FROM flashcards
            WHERE id = ? AND deck_id = ?
            """;

    /**
     * SQL query to get total flashcard count.
     */
    public static final String COUNT_ALL_FLASHCARDS =
            """
            SELECT COUNT(1)
            FROM flashcards
            """;

    /**
     * SQL query to select random flashcards from deck.
     */
    public static final String SELECT_RANDOM_FLASHCARDS_BY_DECK_ID =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            WHERE f.deck_id = ?
            ORDER BY RANDOM()
            LIMIT ?
            """;
}
