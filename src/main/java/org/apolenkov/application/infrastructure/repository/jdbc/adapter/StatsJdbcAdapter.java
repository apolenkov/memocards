package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.StatsSqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of StatsRepository.
 * Handles persistence and retrieval of statistics data using direct JDBC operations.
 */
@Repository
@Profile({"dev", "prod", "test"})
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
    public void appendSession(final SessionStatsDto sessionStats, final LocalDate date) {
        LOGGER.debug("Appending session stats for deck ID: {} on date: {}", sessionStats.deckId(), date);

        // Update or insert daily stats
        jdbcTemplate.update(
                StatsSqlQueries.UPSERT_DAILY_STATS,
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
                jdbcTemplate.update(
                        StatsSqlQueries.INSERT_KNOWN_CARD_IF_NOT_EXISTS,
                        sessionStats.deckId(),
                        cardId,
                        sessionStats.deckId(),
                        cardId);
            }
        }
    }

    /**
     * Gets known card IDs for a deck.
     * This method can be safely overridden by subclasses.
     *
     * @param deckId the deck ID to get known cards for
     * @return set of known card IDs
     */
    @Override
    public Set<Long> getKnownCardIds(final long deckId) {
        LOGGER.debug("Getting known card IDs for deck ID: {}", deckId);
        return new HashSet<>(jdbcTemplate.queryForList(StatsSqlQueries.SELECT_KNOWN_CARD_IDS, Long.class, deckId));
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
    public void setCardKnown(final long deckId, final long cardId, final boolean known) {
        LOGGER.debug("Setting card {} as {} for deck ID: {}", cardId, known ? "known" : "unknown", deckId);

        if (known) {
            jdbcTemplate.update(StatsSqlQueries.INSERT_KNOWN_CARD_IF_NOT_EXISTS, deckId, cardId, deckId, cardId);
        } else {
            jdbcTemplate.update(StatsSqlQueries.DELETE_KNOWN_CARD, deckId, cardId);
        }
    }

    /**
     * Resets progress for a deck.
     * This method can be safely overridden by subclasses.
     *
     * @param deckId the deck ID to reset progress for
     */
    @Override
    public void resetDeckProgress(final long deckId) {
        LOGGER.debug("Resetting progress for deck ID: {}", deckId);

        // Delete daily stats
        jdbcTemplate.update(StatsSqlQueries.DELETE_DAILY_STATS_BY_DECK, deckId);

        // Delete known cards
        jdbcTemplate.update(StatsSqlQueries.DELETE_KNOWN_CARDS_BY_DECK, deckId);
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
    public Map<Long, DeckAggregate> getAggregatesForDecks(final Collection<Long> deckIds, final LocalDate today) {
        LOGGER.debug("Getting aggregates for {} decks on date: {}", deckIds.size(), today);

        if (deckIds.isEmpty()) {
            return new HashMap<>();
        }

        String placeholders =
                deckIds.stream().map(id -> "?").reduce((a, b) -> a + "," + b).orElse("");

        String sql = String.format(StatsSqlQueries.SELECT_AGGREGATES_FOR_DECKS_TEMPLATE, placeholders);

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
