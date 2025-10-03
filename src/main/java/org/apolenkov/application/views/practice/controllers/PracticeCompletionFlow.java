package org.apolenkov.application.views.practice.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.practice.business.PracticePresenter;
import org.apolenkov.application.views.practice.business.PracticeSession;
import org.apolenkov.application.views.practice.components.PracticeActions;
import org.apolenkov.application.views.practice.components.PracticeCard;
import org.apolenkov.application.views.practice.components.PracticeConstants;
import org.apolenkov.application.views.shared.utils.NavigationHelper;

/**
 * Controller for managing practice completion flow.
 * Handles session completion, statistics display, and repeat practice.
 */
public final class PracticeCompletionFlow {

    private final PracticePresenter presenter;
    private final FlashcardUseCase flashcardUseCase;
    private final PracticeCard practiceCard;
    private final PracticeActions practiceActions;

    /**
     * Creates a new PracticeCompletionFlow controller.
     *
     * @param practicePresenter presenter for business logic
     * @param flashcardService use case for flashcard operations
     * @param card component for card display
     * @param actions component for actions
     */
    public PracticeCompletionFlow(
            final PracticePresenter practicePresenter,
            final FlashcardUseCase flashcardService,
            final PracticeCard card,
            final PracticeActions actions) {
        this.presenter = practicePresenter;
        this.flashcardUseCase = flashcardService;
        this.practiceCard = card;
        this.practiceActions = actions;
    }

    /**
     * Handles practice session completion.
     *
     * @param session the completed session
     * @param deck the current deck
     * @return new session for failed cards or null
     */
    public PracticeSession showPracticeComplete(final PracticeSession session, final Deck deck) {
        presenter.recordAndPersist(session);
        createCompletionDisplay(session, deck);
        createCompletionButtons(deck);
        return null; // Session completed
    }

    /**
     * Creates the completion display with session statistics.
     *
     * @param session the completed session
     * @param deck the current deck
     */
    public void createCompletionDisplay(final PracticeSession session, final Deck deck) {
        int total = calculateTotalCards(session);
        long sessionMinutes = calculateSessionMinutes(session);
        long avgSec = calculateAverageSeconds(session);

        practiceCard.displayCompletion(
                deck.getTitle(), session.getCorrectCount(), total, session.getHardCount(), sessionMinutes, avgSec);
    }

    /**
     * Creates completion action buttons.
     *
     * @param deck the current deck
     */
    public void createCompletionButtons(final Deck deck) {
        practiceActions.showCompletionButtons(
                () -> handleRepeatPractice(deck),
                () -> NavigationHelper.navigateToDeck(deck.getId()),
                NavigationHelper::navigateToDecks);
    }

    /**
     * Handles repeat practice for failed cards.
     *
     * @param deck the current deck
     * @return new session with failed cards or null for default practice
     */
    public PracticeSession handleRepeatPractice(final Deck deck) {
        List<Flashcard> failed = getFailedCards(deck);
        practiceActions.resetToPracticeButtons();

        if (failed.isEmpty()) {
            return null; // Trigger default practice
        }

        return startFailedCardsPractice(failed, deck);
    }

    /**
     * Gets list of failed cards that are still not known.
     *
     * @param deck the current deck
     * @return list of failed cards
     */
    public List<Flashcard> getFailedCards(final Deck deck) {
        return flashcardUseCase.getFlashcardsByDeckId(deck.getId()).stream()
                .filter(fc -> isCardFailed(fc, deck))
                .toList();
    }

    /**
     * Starts practice session with failed cards.
     *
     * @param failedCards list of failed cards to practice
     * @param deck the current deck
     * @return new session with failed cards
     */
    public PracticeSession startFailedCardsPractice(final List<Flashcard> failedCards, final Deck deck) {
        PracticeSession session = PracticeSession.create(deck.getId(), new ArrayList<>(failedCards));
        Collections.shuffle(session.getCards());
        return session;
    }

    /**
     * Calculates the total number of cards in the session.
     *
     * @param session the current session
     * @return total number of cards
     */
    private int calculateTotalCards(final PracticeSession session) {
        return (session.getCards() != null) ? session.getCards().size() : session.getTotalViewed();
    }

    /**
     * Calculates session duration in minutes.
     *
     * @param session the current session
     * @return session duration in minutes
     */
    private long calculateSessionMinutes(final PracticeSession session) {
        long sessionMinutes = session.getSessionStart().getEpochSecond();
        sessionMinutes = (java.time.Instant.now().getEpochSecond() - sessionMinutes) / 60;
        return Math.clamp(sessionMinutes, PracticeConstants.MIN_SESSION_MINUTES, PracticeConstants.MAX_SESSION_MINUTES);
    }

    /**
     * Calculates average answer time in seconds.
     *
     * @param session the current session
     * @return average answer time in seconds
     */
    private long calculateAverageSeconds(final PracticeSession session) {
        double denom = Math.clamp(
                session.getTotalViewed(), PracticeConstants.MIN_TOTAL_VIEWED, PracticeConstants.MAX_TOTAL_VIEWED);
        long avgSec = Math.round((session.getTotalAnswerDelayMs() / denom) / 1000.0);
        return Math.clamp(avgSec, PracticeConstants.MIN_AVERAGE_SECONDS, Long.MAX_VALUE);
    }

    /**
     * Checks if a card is failed and still not known.
     *
     * @param flashcard the card to check
     * @param deck the current deck
     * @return true if the card is failed and not known
     */
    private boolean isCardFailed(final Flashcard flashcard, final Deck deck) {
        // This would need access to the current session's failed cards
        // For now, we'll use a simplified approach
        return presenter.getNotKnownCards(deck.getId()).stream()
                .map(Flashcard::getId)
                .collect(Collectors.toSet())
                .contains(flashcard.getId());
    }
}
