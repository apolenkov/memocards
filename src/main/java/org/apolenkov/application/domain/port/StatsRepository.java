package org.apolenkov.application.domain.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Domain port for statistics and progress tracking.
 *
 * <p>Provides operations for managing practice statistics,
 * known cards tracking, and progress aggregation.</p>
 */
public interface StatsRepository {

    /**
     * Records practice session for deck with performance metrics and known card tracking.
     *
     * @param deckId deck identifier
     * @param date practice date
     * @param viewed cards viewed in session
     * @param correct correct answers in session
     * @param repeat repeat attempts in session
     * @param hard hard cards in session
     * @param sessionDurationMs session duration in milliseconds
     * @param totalAnswerDelayMs total answer delay in milliseconds
     * @param knownCardIdsDelta new known card IDs from this session
     */
    @SuppressWarnings({"java:S107", "ParameterNumber"})
    void appendSession(
            long deckId,
            LocalDate date,
            int viewed,
            int correct,
            int repeat,
            int hard,
            long sessionDurationMs,
            long totalAnswerDelayMs,
            Collection<Long> knownCardIdsDelta);

    /**
     * Gets daily statistics for deck.
     *
     * @param deckId deck identifier
     * @return list of daily statistics records
     */
    List<DailyStatsRecord> getDailyStats(long deckId);

    /**
     * Gets known card IDs for deck.
     *
     * @param deckId deck identifier
     * @return set of known card IDs
     */
    Set<Long> getKnownCardIds(long deckId);

    /**
     * Marks card as known or unknown for deck.
     *
     * @param deckId deck identifier
     * @param cardId card identifier
     * @param known true to mark as known, false to mark as unknown
     */
    void setCardKnown(long deckId, long cardId, boolean known);

    /**
     * Resets all progress for deck.
     *
     * @param deckId deck identifier
     */
    void resetDeckProgress(long deckId);

    /**
     * Gets aggregate statistics for multiple decks.
     *
     * @param deckIds deck identifiers
     * @param today current date for today's statistics
     * @return map of deck ID to aggregate statistics
     */
    java.util.Map<Long, DeckAggregate> getAggregatesForDecks(
            java.util.Collection<Long> deckIds, java.time.LocalDate today);

    /**
     * Daily statistics record for specific date.
     */
    record DailyStatsRecord(
            LocalDate date,
            int sessions,
            int viewed,
            int correct,
            int repeat,
            int hard,
            long totalDurationMs,
            long totalAnswerDelayMs) {}

    /**
     * Aggregate statistics for deck (all-time and today).
     */
    record DeckAggregate(
            int sessionsAll,
            int viewedAll,
            int correctAll,
            int repeatAll,
            int hardAll,
            int sessionsToday,
            int viewedToday,
            int correctToday,
            int repeatToday,
            int hardToday) {}
}
