package org.apolenkov.application.domain.port;

import java.time.LocalDate;
import java.util.Collection;
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
     * Gets known card IDs for deck.
     *
     * @param deckId deck identifier
     * @return set of known card IDs
     */
    Set<Long> getKnownCardIds(long deckId);

    /**
     * Checks if specific card is marked as known in deck.
     * More efficient than getKnownCardIds().contains() for single card checks.
     *
     * @param deckId deck identifier
     * @param cardId card identifier
     * @return true if card is marked as known
     */
    boolean isCardKnownDirect(long deckId, long cardId);

    /**
     * Gets known card IDs for multiple decks in single query.
     *
     * @param deckIds collection of deck identifiers (non-null, may be empty)
     * @return map of deck ID to set of known card IDs (decks with no known cards may be absent)
     */
    Map<Long, Set<Long>> getKnownCardIdsBatch(Collection<Long> deckIds);

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
