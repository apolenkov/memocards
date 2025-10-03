package org.apolenkov.application.views.practice.pages;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.exceptions.EntityNotFoundException;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.practice.business.PracticePresenter;
import org.apolenkov.application.views.practice.business.PracticeSession;
import org.apolenkov.application.views.practice.components.PracticeActions;
import org.apolenkov.application.views.practice.components.PracticeAllKnownView;
import org.apolenkov.application.views.practice.components.PracticeCard;
import org.apolenkov.application.views.practice.components.PracticeConstants;
import org.apolenkov.application.views.practice.components.PracticeHeader;
import org.apolenkov.application.views.practice.components.PracticeProgress;
import org.apolenkov.application.views.shared.utils.NavigationHelper;

/**
 * Interactive flashcard practice session view.
 * Provides a complete practice interface for studying flashcards with
 * configurable settings, progress tracking, and session statistics.
 */
@Route(value = RouteConstants.PRACTICE_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_USER)
public class PracticeView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasDynamicTitle {

    // Dependencies
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient PracticePresenter presenter;

    // Data
    private transient Deck currentDeck;
    private transient PracticeSession session;

    // State
    private PracticeDirection sessionDirection = PracticeDirection.FRONT_TO_BACK;

    // UI Components
    private PracticeHeader practiceHeader;
    private PracticeProgress practiceProgress;
    private PracticeCard practiceCard;
    private PracticeActions practiceActions;

    /**
     * Creates a new PracticeView with required dependencies.
     *
     * @param useCase service for flashcard operations
     * @param practicePresenter presenter for managing practice session logic
     */
    public PracticeView(final FlashcardUseCase useCase, final PracticePresenter practicePresenter) {
        this.flashcardUseCase = useCase;
        this.presenter = practicePresenter;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        setupMainLayout();
        createPracticeInterface();
    }

    /**
     * Sets up the main layout properties.
     */
    private void setupMainLayout() {
        getContent().setWidthFull();
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
    }

    /**
     * Creates the complete practice interface.
     */
    private void createPracticeInterface() {
        VerticalLayout contentContainer = createContentContainer();
        VerticalLayout pageSection = createPageSection();

        contentContainer.add(pageSection);
        getContent().add(contentContainer);
    }

    /**
     * Creates the main content container.
     *
     * @return configured content container
     */
    private VerticalLayout createContentContainer() {
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.addClassName(PracticeConstants.CONTAINER_MD_CLASS);
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        return contentContainer;
    }

    /**
     * Creates the main page section with all practice components.
     *
     * @return configured page section
     */
    private VerticalLayout createPageSection() {
        VerticalLayout pageSection = new VerticalLayout();
        pageSection.setSpacing(true);
        pageSection.setPadding(true);
        pageSection.setWidthFull();
        pageSection.addClassName(PracticeConstants.PRACTICE_VIEW_SECTION_CLASS);
        pageSection.addClassName(PracticeConstants.SURFACE_PANEL_CLASS);
        pageSection.addClassName(PracticeConstants.CONTAINER_MD_CLASS);

        initializeComponents();
        setupActionHandlers();

        pageSection.add(practiceHeader, practiceProgress, practiceCard, practiceActions);
        return pageSection;
    }

    /**
     * Initializes all practice components.
     */
    private void initializeComponents() {
        practiceHeader = new PracticeHeader();
        practiceProgress = new PracticeProgress();
        practiceCard = new PracticeCard();
        practiceActions = new PracticeActions();
    }

    /**
     * Sets up action component handlers.
     */
    private void setupActionHandlers() {
        practiceHeader.setBackButtonHandler(this::handleBackToDeck);
        practiceActions.setShowAnswerHandler(this::showAnswer);
        practiceActions.setKnowHandler(() -> markLabeled(PracticeConstants.KNOW_LABEL));
        practiceActions.setHardHandler(() -> markLabeled(PracticeConstants.HARD_LABEL));
    }

    /**
     * Handles back navigation to the deck view.
     * Resets the current session and navigates to the deck.
     */
    private void handleBackToDeck() {
        if (currentDeck != null) {
            // Reset session state
            session = null;

            // Navigate to deck
            NavigationHelper.navigateToDeck(currentDeck.getId());
        }
    }

    /**
     * Sets the deck ID parameter from the URL and initializes practice session.
     *
     * @param event the navigation event containing URL parameters
     * @param parameter the deck ID as a string from the URL
     */
    @Override
    public void setParameter(final BeforeEvent event, final String parameter) {
        try {
            long deckId = parseDeckId(parameter);
            loadDeck(deckId);
            if (currentDeck != null) {
                startDefaultPractice();
            }
        } catch (NumberFormatException e) {
            throw new EntityNotFoundException(
                    parameter, RouteConstants.DECKS_ROUTE, getTranslation(PracticeConstants.PRACTICE_INVALID_ID_KEY));
        }
    }

    /**
     * Parses the deck ID from the URL parameter.
     *
     * @param parameter the deck ID as a string from the URL
     * @return parsed deck ID as long
     * @throws NumberFormatException if the parameter is not a valid number
     */
    private long parseDeckId(final String parameter) throws NumberFormatException {
        return Long.parseLong(parameter);
    }

    private void startDefaultPractice() {
        List<Flashcard> notKnownCards = presenter.getNotKnownCards(currentDeck.getId());
        if (notKnownCards.isEmpty()) {
            showNoCardsOnce();
            return;
        }
        int defaultCount = presenter.resolveDefaultCount(currentDeck.getId());
        boolean random = presenter.isRandom();
        sessionDirection = presenter.defaultDirection();
        startPractice(defaultCount, random);
    }

    private void showNoCardsOnce() {
        showAllKnownLayout();
    }

    /**
     * Shows the all-known layout when all cards are already studied.
     */
    private void showAllKnownLayout() {
        getContent().removeAll();

        PracticeAllKnownView allKnownView = new PracticeAllKnownView(
                currentDeck, getTranslation(PracticeConstants.PRACTICE_TITLE_KEY, currentDeck.getTitle()));

        getContent().add(allKnownView);
    }

    private void loadDeck(final long deckId) {
        Optional<Deck> deckOpt = presenter.loadDeck(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            practiceHeader.setDeckTitle(getTranslation(PracticeConstants.PRACTICE_TITLE_KEY, currentDeck.getTitle()));
        } else {
            throw new EntityNotFoundException(
                    String.valueOf(deckId),
                    RouteConstants.DECKS_ROUTE,
                    getTranslation(PracticeConstants.DECK_NOT_FOUND_KEY));
        }
    }

    private void startPractice(final int count, final boolean random) {
        if (currentDeck == null) {
            return;
        }
        List<Flashcard> filtered = presenter.getNotKnownCards(currentDeck.getId());
        if (filtered.isEmpty()) {
            showNoCardsOnce();
            return;
        }
        session = presenter.startSession(currentDeck.getId(), count, random);
        showCurrentCard();
    }

    private void updateProgress() {
        if (session == null || session.getCards() == null || session.getCards().isEmpty()) {
            return;
        }
        var progress = presenter.progress(session);
        practiceProgress.updateProgress(progress);
    }

    private void showCurrentCard() {
        if (session == null || presenter.isComplete(session)) {
            showPracticeComplete();
            return;
        }

        updateProgress();
        Flashcard currentCard = presenter.currentCard(session);
        session = presenter.startQuestion(session);

        practiceCard.displayQuestionCard(currentCard, sessionDirection);
        practiceActions.showQuestionState();
    }

    private void showAnswer() {
        if (session == null || presenter.isComplete(session)) {
            return;
        }

        Flashcard currentCard = presenter.currentCard(session);
        session = presenter.reveal(session);

        practiceCard.displayAnswerCard(currentCard, sessionDirection);
        practiceActions.showAnswerState();
    }

    private void markLabeled(final String label) {
        if (!isValidSession()) {
            return;
        }

        processCardLabel(label);
        updateProgress();
        practiceActions.hideActionButtons();
        nextCard();
    }

    /**
     * Checks if the current session is valid for marking.
     *
     * @return true if session is valid and showing answer
     */
    private boolean isValidSession() {
        return session != null && session.isShowingAnswer();
    }

    /**
     * Processes the card label (know or hard) through the presenter.
     *
     * @param label the label to process
     */
    private void processCardLabel(final String label) {
        if (PracticeConstants.KNOW_LABEL.equals(label)) {
            session = presenter.markKnow(session);
        } else {
            session = presenter.markHard(session);
        }
    }

    private void nextCard() {
        if (presenter.isComplete(session)) {
            showPracticeComplete();
        } else {
            showCurrentCard();
        }
    }

    private void showPracticeComplete() {
        presenter.recordAndPersist(session);
        createCompletionDisplay();
        createCompletionButtons();
    }

    /**
     * Creates the completion display with session results and statistics.
     */
    private void createCompletionDisplay() {
        int total = calculateTotalCards();
        long sessionMinutes = session.getSessionStart().getEpochSecond();
        sessionMinutes = (java.time.Instant.now().getEpochSecond() - sessionMinutes) / 60; // Convert to minutes
        sessionMinutes = Math.clamp(
                sessionMinutes, PracticeConstants.MIN_SESSION_MINUTES, PracticeConstants.MAX_SESSION_MINUTES);

        double denom = Math.clamp(
                session.getTotalViewed(), PracticeConstants.MIN_TOTAL_VIEWED, PracticeConstants.MAX_TOTAL_VIEWED);
        long avgSec = Math.round((session.getTotalAnswerDelayMs() / denom) / 1000.0);
        avgSec = Math.clamp(avgSec, PracticeConstants.MIN_AVERAGE_SECONDS, Long.MAX_VALUE);

        practiceCard.displayCompletion(
                currentDeck.getTitle(),
                session.getCorrectCount(),
                total,
                session.getHardCount(),
                sessionMinutes,
                avgSec);
    }

    /**
     * Calculates the total number of cards in the session.
     *
     * @return total number of cards
     */
    private int calculateTotalCards() {
        assert session != null;
        return (session.getCards() != null) ? session.getCards().size() : session.getTotalViewed();
    }

    /**
     * Creates completion action buttons for session end.
     */
    private void createCompletionButtons() {
        practiceActions.showCompletionButtons(
                this::handleRepeatPractice,
                () -> NavigationHelper.navigateToDeck(currentDeck.getId()),
                NavigationHelper::navigateToDecks);
    }

    /**
     * Handles repeat practice for failed cards.
     */
    private void handleRepeatPractice() {
        List<Flashcard> failed = getFailedCards();
        practiceActions.resetToPracticeButtons();
        if (failed.isEmpty()) {
            startDefaultPractice();
            return;
        }
        startFailedCardsPractice(failed);
    }

    /**
     * Gets list of failed cards that are still not known.
     *
     * @return list of failed cards
     */
    private List<Flashcard> getFailedCards() {
        return flashcardUseCase.getFlashcardsByDeckId(currentDeck.getId()).stream()
                .filter(fc -> session.getFailedCardIds().contains(fc.getId()))
                .filter(fc -> presenter.getNotKnownCards(currentDeck.getId()).stream()
                        .map(Flashcard::getId)
                        .collect(Collectors.toSet())
                        .contains(fc.getId()))
                .toList();
    }

    /**
     * Starts practice session with failed cards.
     *
     * @param failedCards list of failed cards to practice
     */
    private void startFailedCardsPractice(final List<Flashcard> failedCards) {
        session = PracticeSession.create(currentDeck.getId(), new ArrayList<>(failedCards));
        Collections.shuffle(session.getCards());
        showCurrentCard();
    }

    /**
     * Gets the page title for the practice view.
     *
     * @return the localized practice title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(PracticeConstants.PRACTICE_TITLE_KEY);
    }
}
