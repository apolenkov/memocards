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
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.views.core.exception.EntityNotFoundException;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.practice.business.PracticeSession;
import org.apolenkov.application.views.practice.business.PracticeSessionManager;
import org.apolenkov.application.views.practice.business.PracticeSessionService;
import org.apolenkov.application.views.practice.components.PracticeActions;
import org.apolenkov.application.views.practice.components.PracticeCard;
import org.apolenkov.application.views.practice.components.PracticeCongratulations;
import org.apolenkov.application.views.practice.components.PracticeDisplay;
import org.apolenkov.application.views.practice.constants.PracticeConstants;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interactive flashcard practice session view.
 * Provides a complete practice interface for studying flashcards with
 * configurable settings, progress tracking, and session statistics.
 */
@Route(value = RouteConstants.PRACTICE_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_USER)
public class PracticeView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasDynamicTitle {

    private static final Logger LOGGER = LoggerFactory.getLogger(PracticeView.class);

    // ==================== Fields ====================

    // Dependencies
    private final transient PracticeSessionService sessionService;
    private final transient PracticeSessionManager sessionManager;

    // Data
    private transient Deck currentDeck;
    private transient PracticeSession session;
    private transient PracticeDirection sessionDirection;

    // UI Components
    private PracticeDisplay practiceDisplay;
    private PracticeCard practiceCard;
    private PracticeActions practiceActions;
    private PracticeCongratulations practiceCongratulations;

    // ==================== Constructor ====================

    /**
     * Creates a new PracticeView with required dependencies.
     *
     * @param sessionServiceParam service for session preparation and configuration
     * @param sessionManagerParam manager for active session operations
     */
    public PracticeView(
            final PracticeSessionService sessionServiceParam, final PracticeSessionManager sessionManagerParam) {
        this.sessionService = sessionServiceParam;
        this.sessionManager = sessionManagerParam;
    }

    // ==================== Lifecycle & Initialization ====================

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        // Main layout setup
        getContent().setWidthFull();
        getContent().setPadding(true);
        getContent().setSpacing(true);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);

        // Content container
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.addClassName(PracticeConstants.CONTAINER_MD_CLASS);
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        // Page section
        VerticalLayout pageSection = new VerticalLayout();
        pageSection.setSpacing(true);
        pageSection.setPadding(true);
        pageSection.setWidthFull();
        pageSection.addClassName(PracticeConstants.PRACTICE_VIEW_SECTION_CLASS);
        pageSection.addClassName(PracticeConstants.SURFACE_PANEL_CLASS);
        pageSection.addClassName(PracticeConstants.CONTAINER_MD_CLASS);

        // Initialize components
        initializeComponents();
        pageSection.add(practiceDisplay, practiceCard, practiceActions, practiceCongratulations);
        setupActionHandlers();

        contentContainer.add(pageSection);
        getContent().add(contentContainer);
    }

    /**
     * Initializes all practice components.
     */
    private void initializeComponents() {
        practiceDisplay = new PracticeDisplay();
        practiceCard = new PracticeCard();
        practiceActions = new PracticeActions();
        practiceCongratulations = new PracticeCongratulations("", () -> {});

        // Initially hide congratulations component
        practiceCongratulations.setVisible(false);

        // Load practice direction from user settings
        sessionDirection = sessionService.defaultDirection();
    }

    // ==================== Setup Methods ====================

    /**
     * Sets up action component handlers.
     */
    private void setupActionHandlers() {
        practiceDisplay.setBackButtonHandler(this::handleBackToDeck);
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
            long deckId = Long.parseLong(parameter);
            LOGGER.debug("Initializing practice session for deck ID: {}", deckId);

            loadDeck(deckId);
            if (currentDeck != null) {
                LOGGER.debug(
                        "Starting practice session for deck '{}' (ID: {})",
                        currentDeck.getTitle(),
                        currentDeck.getId());
                startDefaultPractice();
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid deck ID format: {}", parameter, e);
            throw new EntityNotFoundException(
                    parameter, RouteConstants.DECKS_ROUTE, getTranslation(PracticeConstants.PRACTICE_INVALID_ID_KEY));
        }
    }

    // ==================== Practice Session Management ====================

    /**
     * Starts default practice session.
     * Optimized to avoid redundant database queries by reusing fetched cards.
     */
    private void startDefaultPractice() {
        // Fetch not-known cards once
        List<Flashcard> notKnownCards = sessionService.getNotKnownCards(currentDeck.getId());
        if (notKnownCards.isEmpty()) {
            showAllKnownDialogAndRedirect();
            return;
        }

        // Use preloaded cards to avoid redundant SQL query
        int defaultCount = sessionService.resolveDefaultCount(notKnownCards);
        boolean random = sessionService.isRandom();
        session = sessionService.startSession(currentDeck.getId(), defaultCount, random);
        showCurrentCard();
    }

    /**
     * Shows congratulations component when all cards are already studied.
     */
    private void showAllKnownDialogAndRedirect() {
        // Hide practice actions
        if (practiceActions != null) {
            practiceActions.setVisible(false);
        }
        if (practiceCard != null) {
            practiceCard.setVisible(false);
        }

        // Show congratulations component
        if (practiceCongratulations != null) {
            practiceCongratulations.updateContent(
                    currentDeck.getTitle(), () -> NavigationHelper.navigateToDeck(currentDeck.getId()));
            practiceCongratulations.setVisible(true);
        }

        LOGGER.debug("All cards known for deck '{}', showing congratulations component", currentDeck.getTitle());
    }

    /**
     * Loads the deck by ID.
     *
     * @param deckId the deck ID to load
     */
    private void loadDeck(final long deckId) {
        Optional<Deck> deckOpt = sessionService.loadDeck(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            practiceDisplay.setDeckTitle(getTranslation(PracticeConstants.PRACTICE_TITLE_KEY, currentDeck.getTitle()));
            LOGGER.debug("Deck loaded successfully: {} (ID: {})", currentDeck.getTitle(), currentDeck.getId());
        } else {
            LOGGER.warn("Deck not found for ID: {}", deckId);
            throw new EntityNotFoundException(
                    String.valueOf(deckId),
                    RouteConstants.DECKS_ROUTE,
                    getTranslation(PracticeConstants.DECK_NOT_FOUND_KEY));
        }
    }

    /**
     * Shows the current card in question state.
     */
    private void showCurrentCard() {
        if (session == null || sessionManager.isComplete(session)) {
            return;
        }

        updateProgress();
        Flashcard currentCard = sessionManager.currentCard(session);
        sessionManager.startQuestion(session);

        practiceCard.displayQuestionCard(currentCard, sessionDirection);
        practiceActions.showQuestionState();
    }

    /**
     * Shows the answer for the current card.
     */
    private void showAnswer() {
        if (session == null || sessionManager.isComplete(session)) {
            return;
        }

        Flashcard currentCard = sessionManager.currentCard(session);
        session = sessionManager.reveal(session);

        practiceCard.displayAnswerCard(currentCard, sessionDirection);
        practiceActions.showAnswerState();
    }

    /**
     * Marks the current card with the specified label.
     *
     * @param label the label to apply (know or hard)
     */
    private void markLabeled(final String label) {
        if (session == null || !session.isShowingAnswer()) {
            return;
        }

        session = processCardLabel(label);
        updateProgress();
        practiceActions.hideActionButtons();
        nextCard();

        if (sessionManager.isComplete(session)) {
            handlePracticeComplete();
        }
    }

    /**
     * Processes the card label.
     *
     * @param label the label to process
     * @return updated session
     */
    private PracticeSession processCardLabel(final String label) {
        if (PracticeConstants.KNOW_LABEL.equals(label)) {
            return sessionManager.markKnow(session);
        } else {
            return sessionManager.markHard(session);
        }
    }

    /**
     * Moves to the next card or completes the session.
     */
    private void nextCard() {
        if (sessionManager.isComplete(session)) {
            return;
        }
        showCurrentCard();
    }

    /**
     * Updates the progress display.
     */
    private void updateProgress() {
        if (session == null || session.getCards() == null || session.getCards().isEmpty()) {
            return;
        }
        PracticeSessionManager.Progress progress = sessionManager.progress(session);
        practiceDisplay.updateProgress(progress);
    }

    /**
     * Handles practice session completion.
     */
    private void handlePracticeComplete() {
        LOGGER.debug("Completing practice session for deck '{}' (ID: {})", currentDeck.getTitle(), currentDeck.getId());

        sessionManager.recordAndPersist(session, sessionService);
        showCompletionDisplay();
        showCompletionButtons();
        session = null;
    }

    /**
     * Shows completion display with session statistics.
     */
    private void showCompletionDisplay() {
        PracticeSessionService.SessionCompletionMetrics metrics = sessionService.calculateCompletionMetrics(session);

        practiceCard.displayCompletion(
                currentDeck.getTitle(),
                session.getCorrectCount(),
                metrics.totalCards(),
                session.getHardCount(),
                metrics.sessionMinutes(),
                metrics.avgSeconds());
    }

    /**
     * Shows completion action buttons.
     */
    private void showCompletionButtons() {
        List<Flashcard> failedCards = sessionService.getFailedCards(currentDeck.getId(), session.getFailedCardIds());

        Runnable repeatHandler = failedCards.isEmpty() ? null : () -> handleRepeatPractice(failedCards);

        practiceActions.showCompletionButtons(
                repeatHandler,
                () -> NavigationHelper.navigateToDeck(currentDeck.getId()),
                NavigationHelper::navigateToDecks);
    }

    /**
     * Handles repeat practice with new session.
     *
     * @param failedCards list of failed cards to practice
     */
    private void handleRepeatPractice(final List<Flashcard> failedCards) {
        practiceActions.resetToPracticeButtons();
        session = sessionService.startRepeatSession(currentDeck.getId(), failedCards);
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
