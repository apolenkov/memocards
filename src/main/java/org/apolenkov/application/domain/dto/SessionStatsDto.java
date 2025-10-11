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
     * Creates a new builder for SessionStatsDto.
     *
     * @return new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for SessionStatsDto with fluent API.
     * Provides readable, self-documenting construction for 7 parameters.
     */
    public static final class Builder {
        private long deckId;
        private int viewed;
        private int correct;
        private int hard;
        private long sessionDurationMs;
        private long totalAnswerDelayMs;
        private Collection<Long> knownCardIdsDelta;

        private Builder() {
            // Private constructor - use builder() factory method
        }

        /**
         * Sets the deck ID.
         *
         * @param deckIdValue deck identifier
         * @return this builder instance
         */
        public Builder deckId(final long deckIdValue) {
            this.deckId = deckIdValue;
            return this;
        }

        /**
         * Sets the viewed count.
         *
         * @param viewedValue number of cards viewed
         * @return this builder instance
         */
        public Builder viewed(final int viewedValue) {
            this.viewed = viewedValue;
            return this;
        }

        /**
         * Sets the correct count.
         *
         * @param correctValue number of correct answers
         * @return this builder instance
         */
        public Builder correct(final int correctValue) {
            this.correct = correctValue;
            return this;
        }

        /**
         * Sets the hard count.
         *
         * @param hardValue number of cards marked as hard
         * @return this builder instance
         */
        public Builder hard(final int hardValue) {
            this.hard = hardValue;
            return this;
        }

        /**
         * Sets the session duration.
         *
         * @param sessionDurationMsValue session duration in milliseconds
         * @return this builder instance
         */
        public Builder sessionDurationMs(final long sessionDurationMsValue) {
            this.sessionDurationMs = sessionDurationMsValue;
            return this;
        }

        /**
         * Sets the total answer delay.
         *
         * @param totalAnswerDelayMsValue total answer delay in milliseconds
         * @return this builder instance
         */
        public Builder totalAnswerDelayMs(final long totalAnswerDelayMsValue) {
            this.totalAnswerDelayMs = totalAnswerDelayMsValue;
            return this;
        }

        /**
         * Sets the known card IDs delta.
         *
         * @param knownCardIdsDeltaValue collection of card IDs whose knowledge status changed
         * @return this builder instance
         */
        public Builder knownCardIdsDelta(final Collection<Long> knownCardIdsDeltaValue) {
            this.knownCardIdsDelta = knownCardIdsDeltaValue;
            return this;
        }

        /**
         * Builds SessionStatsDto with validation.
         *
         * @return new SessionStatsDto instance
         * @throws IllegalArgumentException if any parameter violates constraints
         */
        public SessionStatsDto build() {
            return new SessionStatsDto(
                    deckId, viewed, correct, hard, sessionDurationMs, totalAnswerDelayMs, knownCardIdsDelta);
        }
    }
}
