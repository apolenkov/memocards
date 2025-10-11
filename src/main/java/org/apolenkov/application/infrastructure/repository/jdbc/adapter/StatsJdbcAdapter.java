package org.apolenkov.application.infrastructure.repository.jdbc.adapter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.infrastructure.repository.jdbc.exception.StatsRetrievalException;
import org.apolenkov.application.infrastructure.repository.jdbc.sql.StatsSqlQueries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
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
        int knownCardsUpdated = 0;
        if (sessionStats.knownCardIdsDelta() != null) {
            for (Long cardId : sessionStats.knownCardIdsDelta()) {
                jdbcTemplate.update(
                        StatsSqlQueries.INSERT_KNOWN_CARD_IF_NOT_EXISTS,
                        sessionStats.deckId(),
                        cardId,
                        sessionStats.deckId(),
                        cardId);
                knownCardsUpdated++;
            }
        }

        LOGGER.debug(
                "Session stats appended: deckId={}, date={}, viewed={}, correct={}, knownCardsUpdated={}",
                sessionStats.deckId(),
                date,
                sessionStats.viewed(),
                sessionStats.correct(),
                knownCardsUpdated);
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
     * Gets known card IDs for multiple decks in single query.
     *
     * @param deckIds collection of deck identifiers
     * @return map of deck ID to set of known card IDs
     */
    @Override
    public Map<Long, Set<Long>> getKnownCardIdsBatch(final Collection<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            return Map.of();
        }

        LOGGER.debug("Batch getting known card IDs for {} decks using single query", deckIds.size());
        try {
            String sql = buildInClauseSql(StatsSqlQueries.SELECT_KNOWN_CARD_IDS_BATCH_TEMPLATE, deckIds.size());
            Map<Long, Set<Long>> result =
                    jdbcTemplate.query(sql, ps -> setLongParameters(ps, deckIds), this::extractKnownCardsByDeck);

            Map<Long, Set<Long>> safeResult = (result != null) ? result : Map.of();
            LOGGER.debug(
                    "Batch known cards completed: {} decks have known cards (out of {} requested)",
                    safeResult.size(),
                    deckIds.size());
            return safeResult;

        } catch (DataAccessException e) {
            throw new StatsRetrievalException("Failed to get known card IDs for deck IDs: " + deckIds, e);
        }
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
        LOGGER.debug("Card {} marked as {} in deck {}", cardId, known ? "known" : "unknown", deckId);
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
        int statsDeleted = jdbcTemplate.update(StatsSqlQueries.DELETE_DAILY_STATS_BY_DECK, deckId);

        // Delete known cards
        int knownCardsDeleted = jdbcTemplate.update(StatsSqlQueries.DELETE_KNOWN_CARDS_BY_DECK, deckId);

        LOGGER.debug(
                "Deck progress reset: deckId={}, statsDeleted={}, knownCardsDeleted={}",
                deckId,
                statsDeleted,
                knownCardsDeleted);
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

        String sql = buildInClauseSql(StatsSqlQueries.SELECT_AGGREGATES_FOR_DECKS_TEMPLATE, deckIds.size());
        Object[] params = buildAggregateParams(today, deckIds);

        Map<Long, DeckAggregate> result = new HashMap<>();
        jdbcTemplate.query(
                sql,
                rs -> {
                    Long deckId = rs.getLong("deck_id");
                    DeckAggregate aggregate = mapToDeckAggregate(rs);
                    result.put(deckId, aggregate);
                },
                params);

        return result;
    }

    /**
     * Builds SQL with IN clause placeholders.
     *
     * @param sqlTemplate SQL template with placeholder for IN clause
     * @param paramCount number of parameters for IN clause
     * @return SQL with placeholders
     */
    private String buildInClauseSql(final String sqlTemplate, final int paramCount) {
        String placeholders = "?,".repeat(Math.max(0, paramCount - 1)) + "?";
        return String.format(sqlTemplate, placeholders);
    }

    /**
     * Sets long parameters in PreparedStatement.
     *
     * @param ps PreparedStatement to set parameters in
     * @param values collection of long values
     */
    private void setLongParameters(final java.sql.PreparedStatement ps, final Collection<Long> values)
            throws java.sql.SQLException {
        int index = 1;
        for (Long value : values) {
            ps.setLong(index++, value);
        }
    }

    /**
     * Extracts known cards by deck from ResultSet.
     *
     * @param rs ResultSet to extract from
     * @return map of deck ID to set of known card IDs
     */
    private Map<Long, Set<Long>> extractKnownCardsByDeck(final java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<Long, Set<Long>> knownCardsByDeck = new HashMap<>();
        while (rs.next()) {
            Long deckId = rs.getLong("deck_id");
            Long cardId = rs.getLong("card_id");
            knownCardsByDeck.computeIfAbsent(deckId, k -> new HashSet<>()).add(cardId);
        }
        return knownCardsByDeck;
    }

    /**
     * Builds parameters array for aggregate query.
     *
     * @param today date for today's statistics
     * @param deckIds collection of deck IDs
     * @return parameters array
     */
    private Object[] buildAggregateParams(final LocalDate today, final Collection<Long> deckIds) {
        Object[] params = new Object[deckIds.size() + 4];
        params[0] = today;
        params[1] = today;
        params[2] = today;
        params[3] = today;
        int index = 4;
        for (Long deckId : deckIds) {
            params[index++] = deckId;
        }
        return params;
    }

    /**
     * Maps ResultSet row to DeckAggregate.
     *
     * @param rs ResultSet positioned at current row
     * @return DeckAggregate from current row
     */
    private DeckAggregate mapToDeckAggregate(final java.sql.ResultSet rs) throws java.sql.SQLException {
        return new DeckAggregate(
                rs.getInt("sessions_all"),
                rs.getInt("viewed_all"),
                rs.getInt("correct_all"),
                rs.getInt("hard_all"),
                rs.getInt("sessions_today"),
                rs.getInt("viewed_today"),
                rs.getInt("correct_today"),
                rs.getInt("hard_today"));
    }
}
