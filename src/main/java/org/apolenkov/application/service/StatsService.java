package org.apolenkov.application.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.port.StatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing flashcard practice statistics and progress tracking.
 *
 * <p>Provides comprehensive statistics tracking for flashcard practice sessions,
 * including daily performance metrics, card knowledge status, and deck progress calculations.</p>
 */
@Service
public class StatsService {

    /**
     * Record representing daily statistics for a deck.
     *
     * <p>Encapsulates all statistics collected for specific deck on specific date,
     * including session counts, card performance metrics, and timing information.</p>
     *
     * @param date date for which statistics are recorded
     * @param sessions number of practice sessions on this date
     * @param viewed total number of cards viewed across all sessions
     * @param correct number of cards answered correctly
     * @param repeat number of cards marked for repetition
     * @param hard number of cards marked as difficult
     * @param totalDurationMs total practice time in milliseconds
     * @param totalAnswerDelayMs total time spent thinking before answering
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
         * Calculates average answer delay per card.
         *
         * <p>Returns average time in milliseconds that users spent thinking
         * before answering each card. Returns 0.0 if no cards were viewed.</p>
         *
         * @return average answer delay in milliseconds, or 0.0 if no cards viewed
         */
        public double getAvgDelayMs() {
            return viewed > 0 ? (double) totalAnswerDelayMs / viewed : 0.0;
        }
    }

    private final StatsRepository statsRepository;

    /**
     * Creates StatsService with required repository dependency.
     *
     * @param statsRepository repository for persisting and retrieving statistics
     */
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Records practice session and updates daily statistics.
     *
     * <p>Records results of single practice session including performance metrics
     * and timing information. Automatically updates daily statistics for specified deck.</p>
     *
     * @param deckId ID of deck being practiced
     * @param viewed number of cards viewed in this session
     * @param correct number of cards answered correctly
     * @param repeat number of cards marked for repetition
     * @param hard number of cards marked as difficult
     * @param sessionDuration total duration of practice session
     * @param totalAnswerDelayMs total time spent thinking before answering
     * @param knownCardIdsDelta collection of card IDs whose knowledge status changed
     */
    @Transactional
    public void recordSession(
            long deckId,
            int viewed,
            int correct,
            int repeat,
            int hard,
            Duration sessionDuration,
            long totalAnswerDelayMs,
            Collection<Long> knownCardIdsDelta) {
        if (viewed <= 0) return;
        LocalDate today = LocalDate.now();
        statsRepository.appendSession(
                deckId,
                today,
                viewed,
                correct,
                repeat,
                hard,
                sessionDuration.toMillis(),
                totalAnswerDelayMs,
                knownCardIdsDelta);
    }

    /**
     * Retrieves daily statistics for specific deck.
     *
     * <p>Returns list of daily statistics records for specified deck, sorted chronologically.
     * Each record contains aggregated metrics for all practice sessions on that date.</p>
     *
     * @param deckId ID of deck to retrieve statistics for
     * @return chronologically sorted list of daily statistics
     */
    @Transactional(readOnly = true)
    public List<DailyStats> getDailyStatsForDeck(long deckId) {
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
     * Calculates progress percentage for deck based on known cards.
     *
     * <p>Computes percentage of cards in deck that user has marked as known.
     * Result is clamped between 0 and 100 percent to ensure valid percentage values.</p>
     *
     * @param deckId ID of deck to calculate progress for
     * @param deckSize total number of cards in deck
     * @return progress percentage (0-100), or 0 if deck size is invalid
     */
    @Transactional(readOnly = true)
    public int getDeckProgressPercent(long deckId, int deckSize) {
        // Handle edge case: invalid deck size
        if (deckSize <= 0) return 0;

        // Calculate percentage of known cards
        int known = statsRepository.getKnownCardIds(deckId).size();
        int percent = (int) Math.round(100.0 * known / deckSize);

        // Clamp percentage to valid range [0, 100]
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;
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
    public boolean isCardKnown(long deckId, long cardId) {
        return statsRepository.getKnownCardIds(deckId).contains(cardId);
    }

    /**
     * Retrieves all card IDs marked as known in specific deck.
     *
     * @param deckId ID of deck to retrieve known cards for
     * @return set of card IDs marked as known
     */
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(long deckId) {
        return statsRepository.getKnownCardIds(deckId);
    }

    /**
     * Sets knowledge status of specific card in deck.
     *
     * <p>Updates knowledge status of card, marking it as either known or unknown
     * based on user's performance and feedback.</p>
     *
     * @param deckId ID of deck containing the card
     * @param cardId ID of card to update
     * @param known true to mark card as known, false to mark as unknown
     */
    @Transactional
    public void setCardKnown(long deckId, long cardId, boolean known) {
        statsRepository.setCardKnown(deckId, cardId, known);
    }

    /**
     * Resets all progress for specific deck.
     *
     * <p>Removes all knowledge status tracking for cards in specified deck,
     * effectively resetting user's progress to zero. Useful when users want
     * to start fresh with a deck.</p>
     *
     * @param deckId ID of deck to reset progress for
     */
    @Transactional
    public void resetDeckProgress(long deckId) {
        statsRepository.resetDeckProgress(deckId);
    }

    /**
     * Retrieves aggregate statistics for multiple decks.
     *
     * <p>Returns aggregated statistics for specified decks including overall
     * performance metrics and progress information. Useful for displaying
     * summary information across multiple decks.</p>
     *
     * @param deckIds list of deck IDs to retrieve aggregates for
     * @param today reference date for calculating aggregates
     * @return map of deck ID to aggregate statistics
     */
    @Transactional(readOnly = true)
    public java.util.Map<Long, org.apolenkov.application.domain.port.StatsRepository.DeckAggregate> getDeckAggregates(
            java.util.List<Long> deckIds, java.time.LocalDate today) {
        return statsRepository.getAggregatesForDecks(deckIds, today);
    }
}
