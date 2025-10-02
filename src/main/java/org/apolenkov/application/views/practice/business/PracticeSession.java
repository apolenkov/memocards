package org.apolenkov.application.views.practice.business;

import java.time.Instant;
import java.util.List;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.views.practice.business.PracticeSessionRecords.SessionData;
import org.apolenkov.application.views.practice.business.PracticeSessionRecords.SessionState;

/**
 * Immutable record representing a practice session.
 * Combines immutable session data with mutable session state.
 *
 * @param data immutable session data (deck ID, cards, timestamps, deltas)
 * @param state mutable session state (index, showing answer, counts, timing)
 */
public record PracticeSession(SessionData data, SessionState state) {

    /**
     * Creates a new practice session with the given deck and cards.
     *
     * @param deckId the ID of the deck being practiced
     * @param cards the list of flashcards for this session
     * @return a new PracticeSession instance
     * @throws IllegalArgumentException if deckId is invalid or cards is null/empty
     */
    public static PracticeSession create(final long deckId, final List<Flashcard> cards) {
        if (deckId <= 0) {
            throw new IllegalArgumentException("Deck ID must be positive");
        }
        if (cards == null) {
            throw new IllegalArgumentException("Cards list cannot be null");
        }
        if (cards.isEmpty()) {
            throw new IllegalArgumentException("Cards list cannot be empty");
        }

        SessionData sessionData = SessionData.create(deckId, cards);
        SessionState sessionState = SessionState.initial();

        return new PracticeSession(sessionData, sessionState);
    }

    /**
     * Gets the deck ID for this practice session.
     *
     * @return the deck ID
     */
    public long getDeckId() {
        return data.deckId();
    }

    /**
     * Gets the list of flashcards for this practice session.
     *
     * @return the list of flashcards
     */
    public List<Flashcard> getCards() {
        return data.cards();
    }

    /**
     * Gets the current card index in the practice session.
     *
     * @return the current card index
     */
    public int getIndex() {
        return state.index();
    }

    /**
     * Checks if the answer is currently being shown.
     *
     * @return true if answer is shown, false otherwise
     */
    public boolean isShowingAnswer() {
        return state.showingAnswer();
    }

    /**
     * Gets the count of correct answers in this session.
     *
     * @return the correct answer count
     */
    public int getCorrectCount() {
        return state.correctCount();
    }

    /**
     * Gets the count of hard cards in this session.
     *
     * @return the hard card count
     */
    public int getHardCount() {
        return state.hardCount();
    }

    /**
     * Gets the total number of cards viewed in this session.
     *
     * @return the total viewed count
     */
    public int getTotalViewed() {
        return state.totalViewed();
    }

    /**
     * Gets the session start timestamp.
     *
     * @return the session start time
     */
    public Instant getSessionStart() {
        return data.sessionStart();
    }

    /**
     * Gets the timestamp when the current card was shown.
     *
     * @return the card show time
     */
    public Instant getCardShowTime() {
        return state.cardShowTime();
    }

    /**
     * Gets the total answer delay in milliseconds for this session.
     *
     * @return the total answer delay in milliseconds
     */
    public long getTotalAnswerDelayMs() {
        return state.totalAnswerDelayMs();
    }

    /**
     * Gets the list of card IDs that became known during this session.
     *
     * @return the list of newly known card IDs
     */
    public List<Long> getKnownCardIdsDelta() {
        return data.knownCardIdsDelta();
    }

    /**
     * Gets the list of card IDs that failed during this session.
     *
     * @return the list of failed card IDs
     */
    public List<Long> getFailedCardIds() {
        return data.failedCardIds();
    }

    /**
     * Creates a new Session with updated state.
     *
     * @param newState the new session state
     * @return a new PracticeSession with updated state
     * @throws IllegalArgumentException if newState is null
     */
    public PracticeSession withState(final SessionState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("Session state cannot be null");
        }
        return new PracticeSession(data, newState);
    }

    /**
     * Creates a new Session with updated data.
     *
     * @param newData the new session data
     * @return a new PracticeSession with updated data
     * @throws IllegalArgumentException if newData is null
     */
    public PracticeSession withData(final SessionData newData) {
        if (newData == null) {
            throw new IllegalArgumentException("Session data cannot be null");
        }
        return new PracticeSession(newData, state);
    }
}
