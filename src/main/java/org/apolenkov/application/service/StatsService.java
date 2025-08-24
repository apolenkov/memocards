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
 * Provides comprehensive statistics tracking for flashcard practice sessions,
 * including daily performance metrics, card knowledge status, and deck progress calculations.
 */
@Service
public class StatsService {

    /**
     * Record representing daily statistics for a deck with session counts, performance metrics, and timing.
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
     * @param statsRepository repository for persisting and retrieving statistics (non-null)
     * @throws IllegalArgumentException if statsRepository is null
     */
    public StatsService(StatsRepository statsRepository) {
        if (statsRepository == null) {
            throw new IllegalArgumentException("StatsRepository cannot be null");
        }
        this.statsRepository = statsRepository;
    }

    /**
     * Records practice session and updates daily statistics.
     *
     * @param deckId ID of deck being practiced (must be positive)
     * @param viewed number of cards viewed in this session (must be non-negative)
     * @param correct number of cards answered correctly (must be non-negative)
     * @param repeat number of cards marked for repetition (must be non-negative)
     * @param hard number of cards marked as difficult (must be non-negative)
     * @param sessionDuration total duration of practice session (non-null, must be positive)
     * @param totalAnswerDelayMs total time spent thinking before answering (must be non-negative)
     * @param knownCardIdsDelta collection of card IDs whose knowledge status changed (may be null or empty)
     * @throws IllegalArgumentException if any parameter violates constraints
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

        // Validate parameters
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        if (correct < 0) {
            throw new IllegalArgumentException("Correct count cannot be negative, got: " + correct);
        }
        if (repeat < 0) {
            throw new IllegalArgumentException("Repeat count cannot be negative, got: " + repeat);
        }
        if (hard < 0) {
            throw new IllegalArgumentException("Hard count cannot be negative, got: " + hard);
        }
        if (sessionDuration == null) {
            throw new IllegalArgumentException("Session duration cannot be null");
        }
        if (totalAnswerDelayMs < 0) {
            throw new IllegalArgumentException("Total answer delay cannot be negative, got: " + totalAnswerDelayMs);
        }

        // Early return if no cards were viewed (including negative values)
        if (viewed <= 0) {
            return;
        }

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
     * Retrieves daily statistics for specific deck, sorted chronologically.
     *
     * @param deckId ID of deck to retrieve statistics for (must be positive)
     * @return chronologically sorted list of daily statistics, never null (may be empty)
     * @throws IllegalArgumentException if deckId is not positive
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
     * Calculates progress percentage for deck based on known cards (0-100%).
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
     * Updates knowledge status of card, marking it as either known or unknown
     * based on user's performance and feedback.
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
     * @param deckId ID of deck to reset progress for
     */
    @Transactional
    public void resetDeckProgress(long deckId) {
        statsRepository.resetDeckProgress(deckId);
    }

    /**
     * Retrieves aggregate statistics for multiple decks with performance metrics and progress information.
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
