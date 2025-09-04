package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.StatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC implementation of StatsRepository.
 * Handles persistence and retrieval of statistics data using direct JDBC operations.
 */
@Repository
@Profile({"dev", "prod"})
public class StatsJdbcAdapter implements StatsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsJdbcAdapter.class);
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates StatsJdbcAdapter with JDBC template.
     *
     * @param jdbcTemplateParam JDBC template for database operations
     */
    public StatsJdbcAdapter(final JdbcTemplate jdbcTemplateParam) {
        this.jdbcTemplate = jdbcTemplateParam;
    }

    /**
     * Appends session statistics to daily stats.
     * This method can be safely overridden by subclasses.
     *
     * @param sessionStats the session statistics to append
     * @param date the date for the statistics
     */
    @Override
    @Transactional
    public void appendSession(final SessionStatsDto sessionStats, final LocalDate date) {
        LOGGER.debug("Appending session stats for deck ID: {} on date: {}", sessionStats.deckId(), date);

        // Update or insert daily stats
        String upsertSql =
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

        jdbcTemplate.update(
                upsertSql,
                sessionStats.deckId(),
                date,
                sessionStats.viewed(),
                sessionStats.correct(),
                sessionStats.hard(),
                sessionStats.sessionDurationMs(),
                sessionStats.totalAnswerDelayMs(),
                sessionStats.viewed(),
                sessionStats.correct(),
                sessionStats.hard(),
                sessionStats.sessionDurationMs(),
                sessionStats.totalAnswerDelayMs());

        // Update known cards if provided
        if (sessionStats.knownCardIdsDelta() != null) {
            for (Long cardId : sessionStats.knownCardIdsDelta()) {
                String knownCardSql =
                        """
                    INSERT INTO known_cards (deck_id, card_id)
                    SELECT ?, ?
                    WHERE NOT EXISTS (
                        SELECT 1 FROM known_cards
                        WHERE deck_id = ? AND card_id = ?
                    )
                    """;
                jdbcTemplate.update(knownCardSql, sessionStats.deckId(), cardId, sessionStats.deckId(), cardId);
            }
        }
    }

    /**
     * Gets daily statistics for a deck.
     * This method can be safely overridden by subclasses.
     *
     * @param deckId the deck ID to get stats for
     * @return list of daily statistics records
     */
    @Override
    @Transactional(readOnly = true)
    public List<DailyStatsRecord> getDailyStats(final long deckId) {
        LOGGER.debug("Getting daily stats for deck ID: {}", deckId);
        String sql =
                """
            SELECT date,
                   sessions,
                   viewed,
                   correct,
                   hard,
                   total_duration_ms,
                   total_delay_ms as total_answer_delay_ms
             FROM deck_daily_stats
             WHERE deck_id = ?
             ORDER BY date DESC
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new DailyStatsRecord(
                        rs.getObject("date", LocalDate.class),
                        rs.getInt("sessions"),
                        rs.getInt("viewed"),
                        rs.getInt("correct"),
                        rs.getInt("hard"),
                        rs.getLong("total_duration_ms"),
                        rs.getLong("total_answer_delay_ms")),
                deckId);
    }

    /**
     * Gets known card IDs for a deck.
     * This method can be safely overridden by subclasses.
     *
     * @param deckId the deck ID to get known cards for
     * @return set of known card IDs
     */
    @Override
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(final long deckId) {
        LOGGER.debug("Getting known card IDs for deck ID: {}", deckId);
        String sql =
                """
            SELECT kc.card_id
            FROM known_cards kc
            JOIN flashcards f ON kc.card_id = f.id
            WHERE f.deck_id = ?
            """;

        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, deckId));
    }

    /**
     * Sets card known status for a deck.
     * This method can be safely overridden by subclasses.
     *
     * @param deckId the deck ID
     * @param cardId the card ID
     * @param known whether the card is known
     */
    @Override
    @Transactional
    public void setCardKnown(final long deckId, final long cardId, final boolean known) {
        LOGGER.debug("Setting card {} as {} for deck ID: {}", cardId, known ? "known" : "unknown", deckId);

        if (known) {
            String insertSql =
                    """
                INSERT INTO known_cards (deck_id, card_id)
                SELECT ?, ?
                WHERE NOT EXISTS (
                    SELECT 1 FROM known_cards
                    WHERE deck_id = ? AND card_id = ?
                )
                """;
            jdbcTemplate.update(insertSql, deckId, cardId, deckId, cardId);
        } else {
            String deleteSql =
                    """
                DELETE FROM known_cards
                WHERE deck_id = ? AND card_id = ?
                """;
            jdbcTemplate.update(deleteSql, deckId, cardId);
        }
    }

    /**
     * Resets progress for a deck.
     * This method can be safely overridden by subclasses.
     *
     * @param deckId the deck ID to reset progress for
     */
    @Override
    @Transactional
    public void resetDeckProgress(final long deckId) {
        LOGGER.debug("Resetting progress for deck ID: {}", deckId);

        // Delete daily stats
        jdbcTemplate.update("DELETE FROM deck_daily_stats WHERE deck_id = ?", deckId);

        // Delete known cards
        String deleteKnownCardsSql =
                """
            DELETE FROM known_cards
            WHERE deck_id = ?
            """;
        jdbcTemplate.update(deleteKnownCardsSql, deckId);
    }

    /**
     * Gets aggregates for multiple decks.
     * This method can be safely overridden by subclasses.
     *
     * @param deckIds collection of deck IDs
     * @param today the date for today's statistics
     * @return map of deck ID to aggregate statistics
     */
    @Override
    @Transactional(readOnly = true)
    public Map<Long, DeckAggregate> getAggregatesForDecks(final Collection<Long> deckIds, final LocalDate today) {
        LOGGER.debug("Getting aggregates for {} decks on date: {}", deckIds.size(), today);

        if (deckIds.isEmpty()) {
            return new HashMap<>();
        }

        String placeholders =
                deckIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("");

        String sql = String.format(
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
            """,
                placeholders);

        Object[] params = new Object[deckIds.size() + 4];
        params[0] = today;
        params[1] = today;
        params[2] = today;
        params[3] = today;
        int i = 4;
        for (Long deckId : deckIds) {
            params[i++] = deckId;
        }

        Map<Long, DeckAggregate> result = new HashMap<>();
        jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    Long deckId = rs.getLong("deck_id");
                    DeckAggregate aggregate = new DeckAggregate(
                            rs.getInt("sessions_all"),
                            rs.getInt("viewed_all"),
                            rs.getInt("correct_all"),
                            rs.getInt("hard_all"),
                            rs.getInt("sessions_today"),
                            rs.getInt("viewed_today"),
                            rs.getInt("correct_today"),
                            rs.getInt("hard_today"));
                    result.put(deckId, aggregate);
                    return null;
                },
                params);

        return result;
    }
}
