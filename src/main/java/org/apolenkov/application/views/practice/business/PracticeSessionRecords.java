package org.apolenkov.application.views.practice.business;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.model.Flashcard;

/**
 * Immutable records for practice session state management.
 * Provides type-safe, immutable data structures for session state.
 */
public final class PracticeSessionRecords {

    /**
     * Immutable session state record.
     * Represents the current state of a practice session.
     *
     * @param index current card index
     * @param showingAnswer whether answer is currently shown
     * @param correctCount number of correct answers
     * @param hardCount number of hard cards
     * @param totalViewed total cards viewed
     * @param cardShowTime when current card was shown
     * @param totalAnswerDelayMs total answer delay in milliseconds
     */
    public record SessionState(
            int index,
            boolean showingAnswer,
            int correctCount,
            int hardCount,
            int totalViewed,
            Instant cardShowTime,
            long totalAnswerDelayMs) {

        /**
         * Creates initial session state.
         *
         * @return initial session state
         */
        public static SessionState initial() {
            return new SessionState(0, false, 0, 0, 0, null, 0L);
        }

        /**
         * Updates the index and resets answer display.
         *
         * @param newIndex the new index
         * @return updated session state
         * @throws IllegalArgumentException if newIndex is negative
         */
        public SessionState withIndex(final int newIndex) {
            if (newIndex < 0) {
                throw new IllegalArgumentException("Index cannot be negative");
            }
            return new SessionState(newIndex, false, correctCount, hardCount, totalViewed, null, totalAnswerDelayMs);
        }

        /**
         * Updates showing answer state.
         *
         * @param showing the new showing state
         * @return updated session state
         */
        public SessionState withShowingAnswer(final boolean showing) {
            return new SessionState(
                    index, showing, correctCount, hardCount, totalViewed, cardShowTime, totalAnswerDelayMs);
        }

        /**
         * Updates card show time.
         *
         * @param showTime the new showtime
         * @return updated session state
         */
        public SessionState withCardShowTime(final Instant showTime) {
            return new SessionState(
                    index, showingAnswer, correctCount, hardCount, totalViewed, showTime, totalAnswerDelayMs);
        }

        /**
         * Updates answer delay.
         *
         * @param delayMs the new delay in milliseconds
         * @return updated session state
         * @throws IllegalArgumentException if delayMs is negative
         */
        public SessionState withAnswerDelay(final long delayMs) {
            if (delayMs < 0) {
                throw new IllegalArgumentException("Answer delay cannot be negative");
            }
            return new SessionState(
                    index,
                    showingAnswer,
                    correctCount,
                    hardCount,
                    totalViewed,
                    cardShowTime,
                    totalAnswerDelayMs + delayMs);
        }

        /**
         * Updates correct count.
         *
         * @param correct the new correct count
         * @return updated session state
         * @throws IllegalArgumentException if correct is negative
         */
        public SessionState withCorrectCount(final int correct) {
            if (correct < 0) {
                throw new IllegalArgumentException("Correct count cannot be negative");
            }
            return new SessionState(
                    index, showingAnswer, correct, hardCount, totalViewed, cardShowTime, totalAnswerDelayMs);
        }

        /**
         * Updates hard count.
         *
         * @param hard the new hard count
         * @return updated session state
         * @throws IllegalArgumentException if hard is negative
         */
        public SessionState withHardCount(final int hard) {
            if (hard < 0) {
                throw new IllegalArgumentException("Hard count cannot be negative");
            }
            return new SessionState(
                    index, showingAnswer, correctCount, hard, totalViewed, cardShowTime, totalAnswerDelayMs);
        }

        /**
         * Updates total viewed count.
         *
         * @param viewed the new total viewed count
         * @return updated session state
         * @throws IllegalArgumentException if viewed is negative
         */
        public SessionState withTotalViewed(final int viewed) {
            if (viewed < 0) {
                throw new IllegalArgumentException("Total viewed cannot be negative");
            }
            return new SessionState(
                    index, showingAnswer, correctCount, hardCount, viewed, cardShowTime, totalAnswerDelayMs);
        }
    }

    /**
     * Immutable session data record.
     * Contains the core session data that doesn't change during practice.
     *
     * @param deckId the deck ID
     * @param cards the list of flashcards
     * @param sessionStart when the session started
     * @param knownCardIdsDelta card IDs that became known
     * @param failedCardIds card IDs that failed
     */
    public record SessionData(
            long deckId,
            List<Flashcard> cards,
            Instant sessionStart,
            List<Long> knownCardIdsDelta,
            List<Long> failedCardIds) {

        /**
         * Creates initial session data.
         *
         * @param deckIdValue the deck ID
         * @param cardsValue the list of flashcards
         * @param sessionStart the session start time
         * @return initial session data
         * @throws IllegalArgumentException if deckId is invalid or cards is null/empty
         */
        public static SessionData create(
                final long deckIdValue, final List<Flashcard> cardsValue, final Instant sessionStart) {
            if (deckIdValue <= 0) {
                throw new IllegalArgumentException("Deck ID must be positive");
            }
            if (cardsValue == null) {
                throw new IllegalArgumentException("Cards list cannot be null");
            }
            if (cardsValue.isEmpty()) {
                throw new IllegalArgumentException("Cards list cannot be empty");
            }
            if (sessionStart == null) {
                throw new IllegalArgumentException("Session start time cannot be null");
            }
            return new SessionData(deckIdValue, cardsValue, sessionStart, new ArrayList<>(), new ArrayList<>());
        }

        /**
         * Adds a known card ID.
         *
         * @param cardId the card ID to add
         * @return updated session data
         */
        public SessionData addKnownCard(final long cardId) {
            List<Long> newKnown = new ArrayList<>(knownCardIdsDelta);
            newKnown.add(cardId);
            return new SessionData(deckId, cards, sessionStart, newKnown, failedCardIds);
        }

        /**
         * Adds a failed card ID.
         *
         * @param cardId the card ID to add
         * @return updated session data
         */
        public SessionData addFailedCard(final long cardId) {
            List<Long> newFailed = new ArrayList<>(failedCardIds);
            newFailed.add(cardId);
            return new SessionData(deckId, cards, sessionStart, knownCardIdsDelta, newFailed);
        }
    }

    // Private constructor to prevent instantiation
    private PracticeSessionRecords() {
        throw new UnsupportedOperationException("Utility class");
    }
}
