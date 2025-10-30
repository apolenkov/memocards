package org.apolenkov.application.infrastructure.repository.jdbc.sql;

/**
 * SQL queries for statistics operations.
 *
 * <p>Contains all SQL queries used by StatsJdbcAdapter.
 * Uses parameterized queries to prevent SQL injection.</p>
 */
public final class StatsSqlQueries {

    private StatsSqlQueries() {
        // Utility class
    }

    /**
     * SQL query to upsert daily statistics for deck.
     */
    public static final String UPSERT_DAILY_STATS =
            """
            INSERT INTO deck_daily_stats (deck_id, date, sessions, viewed, correct, hard, total_duration_ms, total_delay_ms)
            VALUES (?, ?, 1, ?, ?, ?, ?, ?)
            ON CONFLICT (deck_id, date)
            DO UPDATE SET
                sessions = deck_daily_stats.sessions + 1,
                viewed = deck_daily_stats.viewed + ?,
                correct = deck_daily_stats.correct + ?,
                hard = deck_daily_stats.hard + ?,
                total_duration_ms = deck_daily_stats.total_duration_ms + ?,
                total_delay_ms = deck_daily_stats.total_delay_ms + ?
            """;

    /**
     * SQL query to insert known card if not exists.
     */
    public static final String INSERT_KNOWN_CARD_IF_NOT_EXISTS =
            """
            INSERT INTO known_cards (deck_id, card_id)
            SELECT ?, ?
            WHERE NOT EXISTS (
                SELECT 1 FROM known_cards
                WHERE deck_id = ? AND card_id = ?
            )
            """;

    /**
     * SQL query to select known card IDs for deck.
     */
    public static final String SELECT_KNOWN_CARD_IDS =
            """
            SELECT kc.card_id
            FROM known_cards kc
            JOIN cards f ON kc.card_id = f.id
            WHERE f.deck_id = ?
            """;

    /**
     * SQL query to check if specific card is known in deck.
     * More efficient than SELECT_KNOWN_CARD_IDS + contains() for single card checks.
     * Uses EXISTS for optimal performance (stops after first match).
     */
    public static final String IS_CARD_KNOWN_DIRECT =
            """
            SELECT EXISTS(
                SELECT 1
                FROM known_cards kc
                JOIN cards f ON kc.card_id = f.id
                WHERE f.deck_id = ? AND kc.card_id = ?
            ) AS is_known
            """;

    /**
     * SQL query to select known card IDs for multiple decks in single query.
     * Returns both deck_id and card_id for grouping by deck.
     * Note: Requires dynamic SQL for IN clause (deckIds parameter).
     */
    public static final String SELECT_KNOWN_CARD_IDS_BATCH_TEMPLATE =
            """
            SELECT f.deck_id, kc.card_id
            FROM known_cards kc
            JOIN cards f ON kc.card_id = f.id
            WHERE f.deck_id IN (%s)
            """;

    /**
     * SQL query to delete known card.
     */
    public static final String DELETE_KNOWN_CARD =
            """
            DELETE FROM known_cards
            WHERE deck_id = ? AND card_id = ?
            """;

    /**
     * SQL query to delete all daily stats for deck.
     */
    public static final String DELETE_DAILY_STATS_BY_DECK =
            """
            DELETE FROM deck_daily_stats
            WHERE deck_id = ?
            """;

    /**
     * SQL query to delete all known cards for deck.
     */
    public static final String DELETE_KNOWN_CARDS_BY_DECK =
            """
            DELETE FROM known_cards
            WHERE deck_id = ?
            """;

    /**
     * SQL query template for aggregating statistics for multiple decks.
     * Requires placeholders for deck IDs to be formatted at runtime.
     */
    public static final String SELECT_AGGREGATES_FOR_DECKS_TEMPLATE =
            """
            SELECT d.id as deck_id,
                   COALESCE(SUM(dds.sessions), 0) as sessions_all,
                   COALESCE(SUM(dds.viewed), 0) as viewed_all,
                   COALESCE(SUM(dds.correct), 0) as correct_all,

                   COALESCE(SUM(dds.hard), 0) as hard_all,
                   COALESCE(SUM(CASE WHEN dds.date = ? THEN dds.sessions ELSE 0 END), 0) as sessions_today,
                   COALESCE(SUM(CASE WHEN dds.date = ? THEN dds.viewed ELSE 0 END), 0) as viewed_today,
                   COALESCE(SUM(CASE WHEN dds.date = ? THEN dds.correct ELSE 0 END), 0) as correct_today,

                   COALESCE(SUM(CASE WHEN dds.date = ? THEN dds.hard ELSE 0 END), 0) as hard_today
            FROM decks d
            LEFT JOIN deck_daily_stats dds ON d.id = dds.deck_id
            WHERE d.id IN (%s)
            GROUP BY d.id
            """;
}
