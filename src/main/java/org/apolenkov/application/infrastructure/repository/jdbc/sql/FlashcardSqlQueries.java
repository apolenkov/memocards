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
     * SQL query to select flashcards by deck ID with pagination.
     * Uses LIMIT and OFFSET for efficient pagination.
     * Benefits from idx_flashcards_deck_created composite index.
     */
    public static final String SELECT_FLASHCARDS_BY_DECK_ID_PAGINATED =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            WHERE f.deck_id = ?
            ORDER BY f.created_at ASC
            LIMIT ? OFFSET ?
            """;

    /**
     * SQL query to select KNOWN flashcards by deck ID with pagination.
     * Uses LEFT JOIN with known_cards to filter only known cards.
     */
    public static final String SELECT_KNOWN_FLASHCARDS_BY_DECK_ID_PAGINATED =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            INNER JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id
            WHERE f.deck_id = ?
            ORDER BY f.created_at ASC
            LIMIT ? OFFSET ?
            """;

    /**
     * SQL query to select UNKNOWN flashcards by deck ID with pagination.
     * Uses LEFT JOIN with known_cards to filter only unknown cards.
     */
    public static final String SELECT_UNKNOWN_FLASHCARDS_BY_DECK_ID_PAGINATED =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            LEFT JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id
            WHERE f.deck_id = ? AND kc.id IS NULL
            ORDER BY f.created_at ASC
            LIMIT ? OFFSET ?
            """;

    /**
     * SQL query to search flashcards by deck ID and search query with pagination.
     * Searches in front_text, back_text, and example fields (case-insensitive).
     * Uses ILIKE for case-insensitive pattern matching (PostgreSQL).
     */
    public static final String SELECT_FLASHCARDS_BY_DECK_ID_AND_SEARCH_PAGINATED =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            WHERE f.deck_id = ?
              AND (f.front_text ILIKE ? OR f.back_text ILIKE ? OR f.example ILIKE ?)
            ORDER BY f.created_at ASC
            LIMIT ? OFFSET ?
            """;

    /**
     * SQL query to count flashcards matching search query.
     */
    public static final String COUNT_FLASHCARDS_BY_DECK_ID_AND_SEARCH =
            """
            SELECT COUNT(1)
            FROM flashcards f
            WHERE f.deck_id = ?
              AND (f.front_text ILIKE ? OR f.back_text ILIKE ? OR f.example ILIKE ?)
            """;

    /**
     * SQL query to search KNOWN flashcards with pagination.
     * Combines search (ILIKE) with known status (INNER JOIN).
     */
    public static final String SELECT_KNOWN_FLASHCARDS_BY_DECK_ID_AND_SEARCH_PAGINATED =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            INNER JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id
            WHERE f.deck_id = ?
              AND (f.front_text ILIKE ? OR f.back_text ILIKE ? OR f.example ILIKE ?)
            ORDER BY f.created_at ASC
            LIMIT ? OFFSET ?
            """;

    /**
     * SQL query to search UNKNOWN flashcards with pagination.
     * Combines search (ILIKE) with unknown status (LEFT JOIN + IS NULL).
     */
    public static final String SELECT_UNKNOWN_FLASHCARDS_BY_DECK_ID_AND_SEARCH_PAGINATED =
            """
            SELECT f.id, f.deck_id, f.front_text, f.back_text, f.example, f.image_url, f.created_at, f.updated_at
            FROM flashcards f
            LEFT JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id
            WHERE f.deck_id = ? AND kc.id IS NULL
              AND (f.front_text ILIKE ? OR f.back_text ILIKE ? OR f.example ILIKE ?)
            ORDER BY f.created_at ASC
            LIMIT ? OFFSET ?
            """;

    /**
     * SQL query to count KNOWN flashcards matching search query.
     */
    public static final String COUNT_KNOWN_FLASHCARDS_BY_DECK_ID_AND_SEARCH =
            """
            SELECT COUNT(1)
            FROM flashcards f
            INNER JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id
            WHERE f.deck_id = ?
              AND (f.front_text ILIKE ? OR f.back_text ILIKE ? OR f.example ILIKE ?)
            """;

    /**
     * SQL query to count UNKNOWN flashcards matching search query.
     */
    public static final String COUNT_UNKNOWN_FLASHCARDS_BY_DECK_ID_AND_SEARCH =
            """
            SELECT COUNT(1)
            FROM flashcards f
            LEFT JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = f.deck_id
            WHERE f.deck_id = ? AND kc.id IS NULL
              AND (f.front_text ILIKE ? OR f.back_text ILIKE ? OR f.example ILIKE ?)
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
