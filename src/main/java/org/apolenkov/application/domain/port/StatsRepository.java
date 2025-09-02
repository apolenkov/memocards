package org.apolenkov.application.domain.port;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;

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
     * @param sessionStats session statistics data
     * @param date date for the session
     */
    void appendSession(SessionStatsDto sessionStats, LocalDate date);

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
    Map<Long, DeckAggregate> getAggregatesForDecks(Collection<Long> deckIds, LocalDate today);

    /**
     * Daily statistics record for specific date.
     *
     * @param date the date for these statistics
     * @param sessions number of study sessions
     * @param viewed number of cards viewed
     * @param correct number of correct answers
     *
     * @param hard number of cards marked as hard
     * @param totalDurationMs total study duration in milliseconds
     * @param totalAnswerDelayMs total delay before answering in milliseconds
     */
    record DailyStatsRecord(
            LocalDate date,
            int sessions,
            int viewed,
            int correct,
            int hard,
            long totalDurationMs,
            long totalAnswerDelayMs) {}

    /**
     * Aggregate statistics for deck (all-time and today).
     *
     * @param sessionsAll total sessions across all time
     * @param viewedAll total cards viewed across all time
     * @param correctAll total correct answers across all time
     *
     * @param hardAll total hard cards across all time
     * @param sessionsToday sessions for today only
     * @param viewedToday cards viewed today only
     * @param correctToday correct answers today only
     *
     * @param hardToday hard cards today only
     */
    record DeckAggregate(
            int sessionsAll,
            int viewedAll,
            int correctAll,
            int hardAll,
            int sessionsToday,
            int viewedToday,
            int correctToday,
            int hardToday) {}
}
