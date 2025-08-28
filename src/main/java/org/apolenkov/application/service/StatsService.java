package org.apolenkov.application.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.StatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing statistics and progress tracking.
 * Handles session recording, daily statistics aggregation, and card knowledge management.
 */
@Service
public class StatsService {

    private final StatsRepository statsRepository;

    /**
     * Constructs StatsService with required dependencies.
     *
     * @param statsRepositoryValue repository for statistics operations
     */
    public StatsService(final StatsRepository statsRepositoryValue) {
        this.statsRepository = statsRepositoryValue;
    }

    /**
     * Records practice session and updates daily statistics.
     *
     * @param sessionData session data containing all required parameters
     * @throws IllegalArgumentException if any parameter violates constraints
     */
    @Transactional
    public void recordSession(final SessionStatsDto sessionData) {
        // Early return if no cards were viewed (including negative values)
        if (sessionData.viewed() <= 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        statsRepository.appendSession(sessionData, today);
    }

    /**
     * Retrieves daily statistics for specific deck, sorted chronologically.
     *
     * @param deckId ID of deck to retrieve statistics for (must be positive)
     * @return chronologically sorted list of daily statistics, never null (maybe empty)
     * @throws IllegalArgumentException if deckId is not positive
     */
    @Transactional(readOnly = true)
    public List<DailyStats> getDailyStatsForDeck(final long deckId) {
        return statsRepository.getDailyStats(deckId).stream()
                .map(r -> new DailyStats(
                        r.date(),
                        r.sessions(),
                        r.viewed(),
                        r.correct(),
                        r.repeat(),
                        r.hard(),
                        r.totalDurationMs(),
                        r.totalAnswerDelayMs()))
                .sorted(Comparator.comparing(DailyStats::date))
                .toList();
    }

    /**
     * Calculates progress percentage for deck based on known cards (0-100%).
     *
     * @param deckId ID of deck to calculate progress for
     * @param deckSize total number of cards in deck
     * @return progress percentage (0-100), or 0 if deck size is invalid
     */
    @Transactional(readOnly = true)
    public int getDeckProgressPercent(final long deckId, final int deckSize) {
        // Handle edge case: invalid deck size
        if (deckSize <= 0) {
            return 0;
        }

        // Calculate percentage of known cards
        int known = statsRepository.getKnownCardIds(deckId).size();
        int percent = (int) Math.round(100.0 * known / deckSize);

        // Clamp percentage to valid range [0, 100]
        if (percent < 0) {
            percent = 0;
        }
        if (percent > 100) {
            percent = 100;
        }
        return percent;
    }

    /**
     * Checks if specific card is marked as known in deck.
     *
     * @param deckId ID of deck containing the card
     * @param cardId ID of card to check
     * @return true if card is marked as known
     */
    @Transactional(readOnly = true)
    public boolean isCardKnown(final long deckId, final long cardId) {
        return statsRepository.getKnownCardIds(deckId).contains(cardId);
    }

    /**
     * Retrieves all card IDs marked as known in specific deck.
     *
     * @param deckId ID of deck to retrieve known cards for
     * @return set of card IDs marked as known
     */
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(final long deckId) {
        return statsRepository.getKnownCardIds(deckId);
    }

    /**
     * Sets knowledge status of specific card in deck.
     * Updates knowledge status of card, marking it as either known or unknown
     * based on user's performance and feedback.
     *
     * @param deckId ID of deck containing the card
     * @param cardId ID of card to update
     * @param known true to mark card as known, false to mark as unknown
     */
    @Transactional
    public void setCardKnown(final long deckId, final long cardId, final boolean known) {
        statsRepository.setCardKnown(deckId, cardId, known);
    }

    /**
     * Resets all progress for specific deck.
     * Removes all known card associations and resets daily statistics.
     *
     * @param deckId ID of deck to reset progress for
     */
    @Transactional
    public void resetDeckProgress(final long deckId) {
        statsRepository.resetDeckProgress(deckId);
    }

    /**
     * Retrieves aggregated statistics for multiple decks.
     * Provides summary information for dashboard and progress tracking.
     *
     * @param deckIds list of deck IDs to aggregate statistics for
     * @return map of deck ID to aggregated statistics, never null
     */
    @Transactional(readOnly = true)
    public Map<Long, StatsRepository.DeckAggregate> getDeckAggregates(final List<Long> deckIds) {
        if (deckIds == null || deckIds.isEmpty()) {
            return Map.of();
        }
        return statsRepository.getAggregatesForDecks(deckIds, LocalDate.now());
    }

    /**
     * Record for daily statistics with calculated averages.
     * Provides both raw counts and computed metrics for analysis.
     *
     * @param date calendar date for these statistics
     * @param sessions number of practice sessions
     * @param viewed total cards viewed
     * @param correct total correct answers
     * @param repeat total repeat attempts
     * @param hard total hard card markings
     * @param totalDurationMs total session duration in milliseconds
     * @param totalAnswerDelayMs total answer delay in milliseconds
     */
    public record DailyStats(
            LocalDate date,
            int sessions,
            int viewed,
            int correct,
            int repeat,
            int hard,
            long totalDurationMs,
            long totalAnswerDelayMs) {

        /**
         * Calculates average answer delay per card in milliseconds.
         * Returns 0.0 if no cards were viewed to avoid division by zero.
         *
         * @return average delay in milliseconds, or 0.0 if no cards viewed
         */
        public double averageAnswerDelayMs() {
            return viewed > 0 ? (double) totalAnswerDelayMs / viewed : 0.0;
        }
    }
}
