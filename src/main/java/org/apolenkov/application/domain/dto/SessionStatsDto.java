package org.apolenkov.application.domain.dto;

import java.util.Collection;

/**
 * Session statistics for practice tracking.
 * Immutable domain object with validation in compact constructor.
 *
 * @param deckId deck identifier (must be positive)
 * @param viewed number of cards viewed (must be positive)
 * @param correct number of correct answers (must be non-negative)
 * @param hard number of cards marked as hard (must be non-negative)
 * @param sessionDurationMs session duration in milliseconds (must be non-negative)
 * @param totalAnswerDelayMs total answer delay in milliseconds (must be non-negative)
 * @param knownCardIdsDelta collection of card IDs whose knowledge status changed (can be null)
 */
public record SessionStatsDto(
        long deckId,
        int viewed,
        int correct,
        int hard,
        long sessionDurationMs,
        long totalAnswerDelayMs,
        Collection<Long> knownCardIdsDelta) {

    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if any parameter violates constraints
     */
    public SessionStatsDto {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
        }
        if (viewed <= 0) {
            throw new IllegalArgumentException("Viewed count must be positive, got: " + viewed);
        }
        if (correct < 0) {
            throw new IllegalArgumentException("Correct count cannot be negative, got: " + correct);
        }
        if (hard < 0) {
            throw new IllegalArgumentException("Hard count cannot be negative, got: " + hard);
        }
        if (sessionDurationMs < 0) {
            throw new IllegalArgumentException("Session duration cannot be negative, got: " + sessionDurationMs);
        }
        if (totalAnswerDelayMs < 0) {
            throw new IllegalArgumentException("Total answer delay cannot be negative, got: " + totalAnswerDelayMs);
        }
    }

    /**
     * Creates SessionStatsDto with required statistics.
     *
     * @param deckId deck identifier
     * @param viewed number of cards viewed
     * @param correct number of correct answers
     * @param hard number of cards marked as hard
     * @param sessionDurationMs session duration in milliseconds
     * @param totalAnswerDelayMs total answer delay in milliseconds
     * @param knownCardIdsDelta collection of card IDs whose knowledge status changed
     * @return new SessionStatsDto instance
     */
    public static SessionStatsDto of(
            final long deckId,
            final int viewed,
            final int correct,
            final int hard,
            final long sessionDurationMs,
            final long totalAnswerDelayMs,
            final Collection<Long> knownCardIdsDelta) {
        return new SessionStatsDto(
                deckId, viewed, correct, hard, sessionDurationMs, totalAnswerDelayMs, knownCardIdsDelta);
    }
}
