package org.apolenkov.application.views.practice.business;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.springframework.stereotype.Component;

/**
 * Presenter for managing flashcard practice sessions.
 * Coordinates between session service and session manager.
 */
@Component
public final class PracticePresenter {

    // Dependencies
    private final PracticeSessionService sessionService;
    private final PracticeSessionManager sessionManager;

    /**
     * Creates a new PracticePresenter with required dependencies.
     *
     * @param sessionServiceValue service for session preparation and configuration
     * @param sessionManagerValue manager for active session operations
     * @throws IllegalArgumentException if any parameter is null
     */
    public PracticePresenter(
            final PracticeSessionService sessionServiceValue, final PracticeSessionManager sessionManagerValue) {

        if (sessionServiceValue == null) {
            throw new IllegalArgumentException("PracticeSessionService cannot be null");
        }
        if (sessionManagerValue == null) {
            throw new IllegalArgumentException("PracticeSessionManager cannot be null");
        }

        this.sessionService = sessionServiceValue;
        this.sessionManager = sessionManagerValue;
    }

    /**
     * Loads a deck by its ID.
     *
     * @param deckId the ID of the deck to load (must be positive)
     * @return an Optional containing the deck if found, empty otherwise, never null
     * @throws IllegalArgumentException if deckId is not positive
     */
    public Optional<Deck> loadDeck(final long deckId) {
        return sessionService.loadDeck(deckId);
    }

    /**
     * Gets cards that are not yet marked as known in a deck.
     *
     * @param deckId the ID of the deck to check (must be positive)
     * @return a list of flashcards not yet known by the user, never null (maybe empty)
     * @throws IllegalArgumentException if deckId is not positive
     */
    public List<Flashcard> getNotKnownCards(final long deckId) {
        return sessionService.getNotKnownCards(deckId);
    }

    /**
     * Determines the default number of cards for a practice session.
     *
     * @param deckId the ID of the deck to calculate count for (must be positive)
     * @return the number of cards to include in the practice session (1 to configured default)
     * @throws IllegalArgumentException if deckId is not positive
     */
    public int resolveDefaultCount(final long deckId) {
        return sessionService.resolveDefaultCount(deckId);
    }

    /**
     * Determines if practice sessions should use random card order.
     *
     * @return true if random ordering is enabled, false for sequential ordering
     */
    public boolean isRandom() {
        return sessionService.isRandom();
    }

    /**
     * Starts a new practice session.
     *
     * @param deckId the ID of the deck to practice
     * @param count the number of cards to include in the session
     * @param random whether to randomize the card order
     * @return a new Session instance ready for practice
     */
    public PracticeSession startSession(final long deckId, final int count, final boolean random) {
        return sessionService.startSession(deckId, count, random);
    }

    /**
     * Checks if a practice session is complete.
     *
     * @param session the session to check
     * @return true if the session is complete, false otherwise
     */
    public boolean isComplete(final PracticeSession session) {
        return sessionManager.isComplete(session);
    }

    /**
     * Retrieves the current card in the practice session.
     *
     * @param session the session to get the current card from
     * @return the current flashcard, or null if session is complete
     */
    public Flashcard currentCard(final PracticeSession session) {
        return sessionManager.currentCard(session);
    }

    /**
     * Starts a new question in the practice session.
     *
     * @param session the session to start the question for
     */
    public void startQuestion(final PracticeSession session) {
        sessionManager.startQuestion(session);
    }

    /**
     * Reveals the answer for the current card.
     *
     * @param session the session containing the current card
     * @return updated session with answer revealed
     */
    public PracticeSession reveal(final PracticeSession session) {
        return sessionManager.reveal(session);
    }

    /**
     * Marks the current card as known and advances to the next card.
     *
     * @param session the session to update
     * @return updated session with card marked as known
     */
    public PracticeSession markKnow(final PracticeSession session) {
        return sessionManager.markKnow(session);
    }

    /**
     * Marks the current card as difficult and advances to the next card.
     *
     * @param session the session to update
     * @return updated session with card marked as hard
     */
    public PracticeSession markHard(final PracticeSession session) {
        return sessionManager.markHard(session);
    }

    /**
     * Calculates current progress information for a practice session.
     *
     * @param session the session to calculate progress for
     * @return a Progress record with current session metrics
     */
    public PracticeSessionManager.Progress progress(final PracticeSession session) {
        return sessionManager.progress(session);
    }

    /**
     * Records and persists a completed practice session.
     *
     * @param session the completed session to record
     */
    public void recordAndPersist(final PracticeSession session) {
        sessionManager.recordAndPersist(session, sessionService);
    }
}
