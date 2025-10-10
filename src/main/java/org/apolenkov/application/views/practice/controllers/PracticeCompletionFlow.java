package org.apolenkov.application.views.practice.controllers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.views.practice.business.PracticePresenter;
import org.apolenkov.application.views.practice.business.PracticeSession;
import org.apolenkov.application.views.practice.components.PracticeActions;
import org.apolenkov.application.views.practice.components.PracticeCard;
import org.apolenkov.application.views.practice.constants.PracticeConstants;
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
     * @param onRepeatCallback callback to handle repeat with new session
     */
    public void showPracticeComplete(
            final PracticeSession session, final Deck deck, final Consumer<PracticeSession> onRepeatCallback) {
        presenter.recordAndPersist(session);
        createCompletionDisplay(session, deck);
        createCompletionButtons(deck, onRepeatCallback);
    }

    /**
     * Creates the completion display with session statistics.
     *
     * @param session the completed session
     * @param deck the current deck
     */
    public void createCompletionDisplay(final PracticeSession session, final Deck deck) {
        SessionMetrics metrics = calculateSessionMetrics(session);

        practiceCard.displayCompletion(
                deck.getTitle(),
                session.getCorrectCount(),
                metrics.totalCards(),
                session.getHardCount(),
                metrics.sessionMinutes(),
                metrics.avgSeconds());
    }

    /**
     * Creates completion action buttons.
     *
     * @param deck the current deck
     * @param onRepeatCallback callback to handle repeat with new session
     */
    public void createCompletionButtons(final Deck deck, final Consumer<PracticeSession> onRepeatCallback) {
        // Check if there are failed cards to practice
        List<Flashcard> failedCards = getFailedCards(deck);

        Runnable repeatHandler = failedCards.isEmpty()
                ? null
                : () -> handleRepeatPractice(deck).ifPresent(newSession -> {
                    if (onRepeatCallback != null) {
                        onRepeatCallback.accept(newSession);
                    }
                });

        practiceActions.showCompletionButtons(
                repeatHandler, () -> NavigationHelper.navigateToDeck(deck.getId()), NavigationHelper::navigateToDecks);
    }

    /**
     * Handles repeat practice for failed cards.
     *
     * @param deck the current deck
     * @return Optional containing new session for failed cards, empty if no failed cards
     */
    public Optional<PracticeSession> handleRepeatPractice(final Deck deck) {
        List<Flashcard> failed = getFailedCards(deck);
        practiceActions.resetToPracticeButtons();

        if (failed.isEmpty()) {
            return Optional.empty(); // No failed cards to practice
        }

        return Optional.of(startFailedCardsPractice(failed, deck));
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
     * @param deck        the current deck
     * @return new practice session with failed cards
     */
    public PracticeSession startFailedCardsPractice(final List<Flashcard> failedCards, final Deck deck) {
        List<Flashcard> cards = new ArrayList<>(failedCards);
        Collections.shuffle(cards);
        return PracticeSession.create(deck.getId(), cards, Instant.now());
    }

    /**
     * Calculates session metrics including duration and timing statistics.
     *
     * @param session the current session
     * @return session metrics with calculated values
     */
    private SessionMetrics calculateSessionMetrics(final PracticeSession session) {
        int totalCards = (session.getCards() != null) ? session.getCards().size() : session.getTotalViewed();

        long sessionDurationSec =
                Instant.now().getEpochSecond() - session.getSessionStart().getEpochSecond();
        long sessionMinutes = Math.clamp(
                sessionDurationSec / 60, PracticeConstants.MIN_SESSION_MINUTES, PracticeConstants.MAX_SESSION_MINUTES);

        double denom = Math.clamp(
                session.getTotalViewed(), PracticeConstants.MIN_TOTAL_VIEWED, PracticeConstants.MAX_TOTAL_VIEWED);
        long avgSeconds = Math.clamp(
                Math.round((session.getTotalAnswerDelayMs() / denom) / 1000.0),
                PracticeConstants.MIN_AVERAGE_SECONDS,
                Long.MAX_VALUE);

        return new SessionMetrics(totalCards, sessionMinutes, avgSeconds);
    }

    /**
     * Record for session metrics calculations.
     *
     * @param totalCards total number of cards
     * @param sessionMinutes session duration in minutes
     * @param avgSeconds average answer time in seconds
     */
    private record SessionMetrics(int totalCards, long sessionMinutes, long avgSeconds) {}

    /**
     * Checks if a card is not yet known by the user.
     *
     * @param flashcard the card to check
     * @param deck the current deck
     * @return true if the card is not known
     */
    private boolean isCardFailed(final Flashcard flashcard, final Deck deck) {
        return presenter.getNotKnownCards(deck.getId()).stream()
                .map(Flashcard::getId)
                .collect(Collectors.toSet())
                .contains(flashcard.getId());
    }
}
