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
 * <p>This service provides comprehensive statistics tracking for flashcard practice
 * sessions, including daily performance metrics, card knowledge status, and deck
 * progress calculations. It handles both recording new session data and querying
 * historical statistics for analysis and progress display.</p>
 *
 * <p>The service maintains statistics at both the session level (individual practice
 * sessions) and the aggregate level (daily and overall deck progress). It tracks
 * metrics such as cards viewed, correct answers, repeat requests, difficulty ratings,
 * and timing information.</p>
 *
 *
 */
@Service
public class StatsService {

    /**
     * Record representing daily statistics for a deck.
     *
     * <p>This record encapsulates all statistics collected for a specific deck
     * on a specific date, including session counts, card performance metrics,
     * and timing information.</p>
     *
     * @param date the date for which statistics are recorded
     * @param sessions the number of practice sessions on this date
     * @param viewed the total number of cards viewed across all sessions
     * @param correct the number of cards answered correctly
     * @param repeat the number of cards marked for repetition
     * @param hard the number of cards marked as difficult
     * @param totalDurationMs the total practice time in milliseconds
     * @param totalAnswerDelayMs the total time spent thinking before answering
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
         * Calculates the average answer delay per card.
         *
         * <p>Returns the average time in milliseconds that users spent thinking
         * before answering each card. Returns 0.0 if no cards were viewed.</p>
         *
         * @return the average answer delay in milliseconds, or 0.0 if no cards viewed
         */
        public double getAvgDelayMs() {
            return viewed > 0 ? (double) totalAnswerDelayMs / viewed : 0.0;
        }
    }

    private final StatsRepository statsRepository;

    /**
     * Constructs a new StatsService with the required repository dependency.
     *
     * @param statsRepository the repository for persisting and retrieving statistics
     */
    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    /**
     * Records a practice session and updates daily statistics.
     *
     * <p>This method records the results of a single practice session, including
     * performance metrics and timing information. It automatically updates daily
     * statistics for the specified deck and tracks card knowledge status changes.</p>
     *
     * <p>If no cards were viewed during the session, the method returns early
     * without making any changes to the statistics.</p>
     *
     * @param deckId the ID of the deck being practiced
     * @param viewed the number of cards viewed in this session
     * @param correct the number of cards answered correctly
     * @param repeat the number of cards marked for repetition
     * @param hard the number of cards marked as difficult
     * @param sessionDuration the total duration of the practice session
     * @param totalAnswerDelayMs the total time spent thinking before answering
     * @param knownCardIdsDelta the collection of card IDs whose knowledge status changed
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
     * Retrieves daily statistics for a specific deck.
     *
     * <p>Returns a list of daily statistics records for the specified deck,
     * sorted chronologically by date. Each record contains aggregated metrics
     * for all practice sessions on that particular date.</p>
     *
     * @param deckId the ID of the deck to retrieve statistics for
     * @return a chronologically sorted list of daily statistics
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
     * Calculates the progress percentage for a deck based on known cards.
     *
     * <p>Computes the percentage of cards in a deck that the user has marked
     * as known. The result is clamped between 0 and 100 percent to ensure
     * valid percentage values.</p>
     *
     * @param deckId the ID of the deck to calculate progress for
     * @param deckSize the total number of cards in the deck
     * @return the progress percentage (0-100), or 0 if deck size is invalid
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
     * Checks if a specific card is marked as known in a deck.
     *
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to check
     * @return true if the card is marked as known, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isCardKnown(long deckId, long cardId) {
        return statsRepository.getKnownCardIds(deckId).contains(cardId);
    }

    /**
     * Retrieves all card IDs marked as known in a specific deck.
     *
     * @param deckId the ID of the deck to retrieve known cards for
     * @return a set of card IDs that are marked as known
     */
    @Transactional(readOnly = true)
    public Set<Long> getKnownCardIds(long deckId) {
        return statsRepository.getKnownCardIds(deckId);
    }

    /**
     * Sets the knowledge status of a specific card in a deck.
     *
     * <p>Updates the knowledge status of a card, marking it as either known
     * or unknown based on the user's performance and feedback.</p>
     *
     * @param deckId the ID of the deck containing the card
     * @param cardId the ID of the card to update
     * @param known true to mark the card as known, false to mark as unknown
     */
    @Transactional
    public void setCardKnown(long deckId, long cardId, boolean known) {
        statsRepository.setCardKnown(deckId, cardId, known);
    }

    /**
     * Resets all progress for a specific deck.
     *
     * <p>Removes all knowledge status tracking for cards in the specified deck,
     * effectively resetting the user's progress to zero. This is useful when
     * users want to start fresh with a deck.</p>
     *
     * @param deckId the ID of the deck to reset progress for
     */
    @Transactional
    public void resetDeckProgress(long deckId) {
        statsRepository.resetDeckProgress(deckId);
    }

    /**
     * Retrieves aggregate statistics for multiple decks.
     *
     * <p>Returns aggregated statistics for the specified decks, including
     * overall performance metrics and progress information. This method is
     * useful for displaying summary information across multiple decks.</p>
     *
     * @param deckIds the list of deck IDs to retrieve aggregates for
     * @param today the reference date for calculating aggregates
     * @return a map of deck ID to aggregate statistics
     */
    @Transactional(readOnly = true)
    public java.util.Map<Long, org.apolenkov.application.domain.port.StatsRepository.DeckAggregate> getDeckAggregates(
            java.util.List<Long> deckIds, java.time.LocalDate today) {
        return statsRepository.getAggregatesForDecks(deckIds, today);
    }
}
