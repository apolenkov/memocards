package org.apolenkov.application.views.practice.controllers;

import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.views.practice.business.PracticePresenter;
import org.apolenkov.application.views.practice.business.PracticeSession;
import org.apolenkov.application.views.practice.components.PracticeActions;
import org.apolenkov.application.views.practice.components.PracticeCard;
import org.apolenkov.application.views.practice.components.PracticeConstants;
import org.apolenkov.application.views.practice.components.PracticeProgress;

/**
 * Controller for managing practice session flow.
 * Handles session start, card progression, and user interactions.
 */
public final class PracticeSessionFlow {

    private final PracticePresenter presenter;
    private final PracticeCard practiceCard;
    private final PracticeProgress practiceProgress;
    private final PracticeActions practiceActions;

    /**
     * Creates a new PracticeSessionFlow controller.
     *
     * @param practicePresenter presenter for business logic
     * @param card component for card display
     * @param progress component for progress display
     * @param actions component for actions
     */
    public PracticeSessionFlow(
            final PracticePresenter practicePresenter,
            final PracticeCard card,
            final PracticeProgress progress,
            final PracticeActions actions) {
        this.presenter = practicePresenter;
        this.practiceCard = card;
        this.practiceProgress = progress;
        this.practiceActions = actions;
    }

    /**
     * Starts default practice session with configured settings.
     *
     * @param deck the current deck
     * @param sessionDirection the practice direction
     * @return the created session or null if no cards available
     */
    public PracticeSession startDefaultPractice(final Deck deck, final PracticeDirection sessionDirection) {
        List<Flashcard> notKnownCards = presenter.getNotKnownCards(deck.getId());
        if (notKnownCards.isEmpty()) {
            return null;
        }

        int defaultCount = presenter.resolveDefaultCount(deck.getId());
        boolean random = presenter.isRandom();
        return startPractice(deck, defaultCount, random, sessionDirection);
    }

    /**
     * Starts practice session with specified parameters.
     *
     * @param deck the current deck
     * @param count number of cards to practice
     * @param random whether to randomize card order
     * @param sessionDirection the practice direction
     * @return the created session or null if no cards available
     */
    public PracticeSession startPractice(
            final Deck deck, final int count, final boolean random, final PracticeDirection sessionDirection) {
        List<Flashcard> filtered = presenter.getNotKnownCards(deck.getId());
        if (filtered.isEmpty()) {
            return null;
        }

        PracticeSession session = presenter.startSession(deck.getId(), count, random);
        showCurrentCard(session, sessionDirection);
        return session;
    }

    /**
     * Shows the current card in question state.
     *
     * @param session the current session
     * @param sessionDirection the practice direction
     */
    public void showCurrentCard(final PracticeSession session, final PracticeDirection sessionDirection) {
        if (session == null || presenter.isComplete(session)) {
            return;
        }

        updateProgress(session);
        Flashcard currentCard = presenter.currentCard(session);
        presenter.startQuestion(session);

        practiceCard.displayQuestionCard(currentCard, sessionDirection);
        practiceActions.showQuestionState();
    }

    /**
     * Shows the answer for the current card.
     *
     * @param session the current session
     * @param sessionDirection the practice direction
     * @return updated session
     */
    public PracticeSession showAnswer(final PracticeSession session, final PracticeDirection sessionDirection) {
        if (session == null || presenter.isComplete(session)) {
            return session;
        }

        Flashcard currentCard = presenter.currentCard(session);
        PracticeSession updatedSession = presenter.reveal(session);

        practiceCard.displayAnswerCard(currentCard, sessionDirection);
        practiceActions.showAnswerState();
        return updatedSession;
    }

    /**
     * Marks the current card with the specified label.
     *
     * @param session the current session
     * @param label the label to apply (know or hard)
     * @param sessionDirection the practice direction
     * @return updated session
     */
    public PracticeSession markLabeled(
            final PracticeSession session, final String label, final PracticeDirection sessionDirection) {
        if (!isValidSession(session)) {
            return session;
        }

        PracticeSession updatedSession = processCardLabel(session, label);
        updateProgress(updatedSession);
        practiceActions.hideActionButtons();
        nextCard(updatedSession, sessionDirection);
        return updatedSession;
    }

    /**
     * Moves to the next card or completes the session.
     *
     * @param session the current session
     * @param sessionDirection the practice direction
     */
    public void nextCard(final PracticeSession session, final PracticeDirection sessionDirection) {
        if (presenter.isComplete(session)) {
            return; // Completion handled by caller
        }
        showCurrentCard(session, sessionDirection);
    }

    /**
     * Checks if the session is valid for marking.
     *
     * @param session the current session
     * @return true if valid for marking
     */
    private boolean isValidSession(final PracticeSession session) {
        return session != null && session.isShowingAnswer();
    }

    /**
     * Processes the card label through the presenter.
     *
     * @param session the current session
     * @param label the label to process
     * @return updated session
     */
    private PracticeSession processCardLabel(final PracticeSession session, final String label) {
        if (PracticeConstants.KNOW_LABEL.equals(label)) {
            return presenter.markKnow(session);
        } else {
            return presenter.markHard(session);
        }
    }

    /**
     * Updates the progress display.
     *
     * @param session the current session
     */
    private void updateProgress(final PracticeSession session) {
        if (session == null || session.getCards() == null || session.getCards().isEmpty()) {
            return;
        }
        var progress = presenter.progress(session);
        practiceProgress.updateProgress(progress);
    }
}
