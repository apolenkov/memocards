package org.apolenkov.application.domain.dto;

import java.util.Collection;

/**
 * Data Transfer Object for session statistics parameters.
 * Encapsulates all parameters needed for recording a study session.
 * Can be used for both recording sessions and accumulating statistics.
 *
 * @param deckId deck identifier (must be positive)
 * @param viewed number of cards viewed (must be positive)
 * @param correct number of correct answers (must be non-negative)
 * @param repeat number of cards marked for repetition (must be non-negative)
 * @param hard number of cards marked as hard (must be non-negative)
 * @param sessionDurationMs session duration in milliseconds (must be non-negative)
 * @param totalAnswerDelayMs total answer delay in milliseconds (must be non-negative)
 * @param knownCardIdsDelta collection of card IDs whose knowledge status changed (can be null)
 */
public record SessionStatsDto(
        long deckId,
        int viewed,
        int correct,
        int repeat,
        int hard,
        long sessionDurationMs,
        long totalAnswerDelayMs,
        Collection<Long> knownCardIdsDelta) {

    /**
     * Builder class for SessionStatsDto to avoid too many parameters.
     */
    public static final class Builder {
        private long deckId;
        private int viewed;
        private int correct;
        private int repeat;
        private int hard;
        private long sessionDurationMs;
        private long totalAnswerDelayMs;
        private Collection<Long> knownCardIdsDelta;

        /**
         * Sets the deck identifier.
         *
         * @param deckIdValue deck identifier
         * @return this builder instance
         */
        public Builder deckId(final long deckIdValue) {
            this.deckId = deckIdValue;
            return this;
        }

        /**
         * Sets the number of cards viewed.
         *
         * @param viewedValue number of cards viewed
         * @return this builder instance
         */
        public Builder viewed(final int viewedValue) {
            this.viewed = viewedValue;
            return this;
        }

        /**
         * Sets the number of correct answers.
         *
         * @param correctValue number of correct answers
         * @return this builder instance
         */
        public Builder correct(final int correctValue) {
            this.correct = correctValue;
            return this;
        }

        /**
         * Sets the number of cards marked for repetition.
         *
         * @param repeatValue number of cards marked for repetition
         * @return this builder instance
         */
        public Builder repeat(final int repeatValue) {
            this.repeat = repeatValue;
            return this;
        }

        /**
         * Sets the number of cards marked as hard.
         *
         * @param hardValue number of cards marked as hard
         * @return this builder instance
         */
        public Builder hard(final int hardValue) {
            this.hard = hardValue;
            return this;
        }

        /**
         * Sets the session duration in milliseconds.
         *
         * @param sessionDurationMsValue session duration in milliseconds
         * @return this builder instance
         */
        public Builder sessionDurationMs(final long sessionDurationMsValue) {
            this.sessionDurationMs = sessionDurationMsValue;
            return this;
        }

        /**
         * Sets the total answer delay in milliseconds.
         *
         * @param totalAnswerDelayMsValue total answer delay in milliseconds
         * @return this builder instance
         */
        public Builder totalAnswerDelayMs(final long totalAnswerDelayMsValue) {
            this.totalAnswerDelayMs = totalAnswerDelayMsValue;
            return this;
        }

        /**
         * Sets the collection of card IDs whose knowledge status changed.
         *
         * @param knownCardIdsDeltaValue collection of card IDs whose knowledge status changed
         * @return this builder instance
         */
        public Builder knownCardIdsDelta(final Collection<Long> knownCardIdsDeltaValue) {
            this.knownCardIdsDelta = knownCardIdsDeltaValue;
            return this;
        }

        /**
         * Builds and returns a new SessionStatsDto instance.
         *
         * @return new SessionStatsDto instance
         * @throws IllegalArgumentException if any parameter violates constraints
         */
        public SessionStatsDto build() {
            validateParameters();
            return new SessionStatsDto(
                    deckId, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs, knownCardIdsDelta);
        }

        /**
         * Validates all parameters for session statistics.
         *
         * @throws IllegalArgumentException if any parameter violates constraints
         */
        private void validateParameters() {
            if (deckId <= 0) {
                throw new IllegalArgumentException("Deck ID must be positive, got: " + deckId);
            }
            if (viewed <= 0) {
                throw new IllegalArgumentException("Viewed count must be positive, got: " + viewed);
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
            if (sessionDurationMs < 0) {
                throw new IllegalArgumentException("Session duration cannot be negative, got: " + sessionDurationMs);
            }
            if (totalAnswerDelayMs < 0) {
                throw new IllegalArgumentException("Total answer delay cannot be negative, got: " + totalAnswerDelayMs);
            }
        }
    }

    /**
     * Creates a new builder instance.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
