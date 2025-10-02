package org.apolenkov.application.views.practice.pages;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
import org.apolenkov.application.views.practice.components.PracticeConstants;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

/**
 * Interactive flashcard practice session view.
 * Provides a complete practice interface for studying flashcards with
 * configurable settings, progress tracking, and session statistics.
 */
@Route(value = RouteConstants.PRACTICE_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_USER)
public class PracticeView extends Composite<VerticalLayout> implements HasUrlParameter<String>, HasDynamicTitle {

    private final transient FlashcardUseCase flashcardUseCase;
    private final transient PracticePresenter presenter;
    private transient Deck currentDeck;
    private transient PracticePresenter.Session session;

    private boolean noCardsNotified = false;

    // session stats
    private int correctCount = 0;
    private int hardCount = 0;
    private int totalViewed = 0;

    private PracticeDirection sessionDirection = PracticeDirection.FRONT_TO_BACK;

    // UI Components
    private H2 deckTitle;
    private Div cardContent;
    private HorizontalLayout actionButtons;
    private Button showAnswerButton;
    private Button knowButton;
    private Button hardButton;
    private Span statsSpan;

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

        createHeader(pageSection);
        createProgressSection(pageSection);
        createCardContainer(pageSection);
        createActionButtons(pageSection);

        return pageSection;
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
        if (!noCardsNotified) {
            noCardsNotified = true;
            NotificationHelper.showInfo(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_KEY));
        }
    }

    private void createHeader(final VerticalLayout container) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout leftSection = new HorizontalLayout();
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.COMMON_BACK_KEY),
                VaadinIcon.ARROW_LEFT,
                e -> {
                    if (currentDeck != null) {
                        NavigationHelper.navigateToDeck(currentDeck.getId());
                    } else {
                        // If no deck is loaded, navigate to decks list
                        NavigationHelper.navigateToDecks();
                    }
                },
                ButtonVariant.LUMO_TERTIARY);
        backButton.setText(getTranslation(PracticeConstants.PRACTICE_BACK_KEY));

        deckTitle = new H2(getTranslation(PracticeConstants.PRACTICE_TITLE_KEY));
        deckTitle.addClassName(PracticeConstants.PRACTICE_VIEW_DECK_TITLE_CLASS);

        leftSection.add(backButton, deckTitle);

        headerLayout.add(leftSection);
        container.add(headerLayout);
    }

    private void createProgressSection(final VerticalLayout container) {
        Div progressSection = new Div();
        progressSection.addClassName(PracticeConstants.PRACTICE_PROGRESS_CLASS);

        statsSpan = new Span(getTranslation(PracticeConstants.PRACTICE_GET_READY_KEY));
        statsSpan.addClassName(PracticeConstants.PRACTICE_PROGRESS_TEXT_CLASS);
        progressSection.add(statsSpan);

        container.add(progressSection);
    }

    private void createCardContainer(final VerticalLayout container) {
        Div cardContainer = new Div();
        cardContainer.addClassName(PracticeConstants.PRACTICE_CARD_CONTAINER_CLASS);

        cardContent = new Div();
        cardContent.addClassName(PracticeConstants.PRACTICE_CARD_CONTENT_CLASS);

        cardContent.add(new Span(getTranslation(PracticeConstants.PRACTICE_LOADING_CARDS_KEY)));
        cardContainer.add(cardContent);

        container.add(cardContainer);
    }

    private void createActionButtons(final VerticalLayout container) {
        actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.setWidthFull();
        actionButtons.setAlignItems(FlexComponent.Alignment.CENTER);
        actionButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        showAnswerButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_SHOW_ANSWER_KEY),
                e -> showAnswer(),
                ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_LARGE);

        knowButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_KNOW_KEY),
                e -> markLabeled(PracticeConstants.KNOW_LABEL),
                ButtonVariant.LUMO_SUCCESS,
                ButtonVariant.LUMO_LARGE);
        knowButton.setVisible(false);

        hardButton = ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_HARD_KEY),
                e -> markLabeled(PracticeConstants.HARD_LABEL),
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_LARGE);
        hardButton.setVisible(false);

        actionButtons.add(showAnswerButton, knowButton, hardButton);
        container.add(actionButtons);
    }

    private void loadDeck(final long deckId) {
        Optional<Deck> deckOpt = presenter.loadDeck(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            deckTitle.setText(getTranslation(PracticeConstants.PRACTICE_TITLE_KEY, currentDeck.getTitle()));
        } else {
            // Just throw the exception - it will be caught by EntityNotFoundErrorHandler
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
        correctCount = 0;
        hardCount = 0;
        totalViewed = 0;
        noCardsNotified = false;
        showCurrentCard();
    }

    private void updateProgress() {
        if (session == null || session.getCards() == null || session.getCards().isEmpty()) {
            return;
        }
        var p = presenter.progress(session);
        // Calculate progress based on completed cards, not current position
        int completedCards = p.totalViewed();
        int totalCards = p.total();
        int percent = totalCards > 0 ? Math.round((float) completedCards / totalCards * 100) : 0;

        statsSpan.setText(getTranslation(
                PracticeConstants.PRACTICE_PROGRESS_LINE_KEY,
                p.current(),
                p.total(),
                p.totalViewed(),
                p.correct(),
                p.hard(),
                percent));
    }

    private String getQuestionText(final Flashcard card) {
        return sessionDirection == PracticeDirection.BACK_TO_FRONT ? card.getBackText() : card.getFrontText();
    }

    private String getAnswerText(final Flashcard card) {
        return sessionDirection == PracticeDirection.BACK_TO_FRONT ? card.getFrontText() : card.getBackText();
    }

    private void showCurrentCard() {
        if (session == null || presenter.isComplete(session)) {
            showPracticeComplete();
            return;
        }

        updateProgress();
        Flashcard currentCard = presenter.currentCard(session);
        presenter.startQuestion(session);

        displayQuestionCard(currentCard);
        updateActionButtonsForQuestion();
    }

    /**
     * Displays the question card with the current flashcard.
     *
     * @param currentCard the current flashcard to display
     */
    private void displayQuestionCard(final Flashcard currentCard) {
        cardContent.removeAll();

        VerticalLayout cardLayout = createCardLayout();
        H1 question = new H1(Optional.ofNullable(getQuestionText(currentCard)).orElse(""));
        Span transcription = new Span(" ");

        cardLayout.add(question, transcription);
        cardContent.add(cardLayout);
    }

    /**
     * Creates a card layout for displaying flashcard content.
     *
     * @return configured card layout
     */
    private VerticalLayout createCardLayout() {
        VerticalLayout cardLayout = new VerticalLayout();
        cardLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        cardLayout.setSpacing(true);
        return cardLayout;
    }

    /**
     * Updates action buttons visibility for question state.
     */
    private void updateActionButtonsForQuestion() {
        showAnswerButton.setVisible(true);
        knowButton.setVisible(false);
        hardButton.setVisible(false);
    }

    private void showAnswer() {
        if (session == null || presenter.isComplete(session)) {
            return;
        }

        Flashcard currentCard = presenter.currentCard(session);
        presenter.reveal(session);

        displayAnswerCard(currentCard);
        updateActionButtonsForAnswer();
    }

    /**
     * Displays the answer card with question, divider, answer and optional example.
     *
     * @param currentCard the current flashcard to display
     */
    private void displayAnswerCard(final Flashcard currentCard) {
        cardContent.removeAll();

        VerticalLayout cardLayout = createCardLayout();
        H2 question = new H2(Optional.ofNullable(getQuestionText(currentCard)).orElse(""));
        Hr divider = new Hr();
        H1 answer = new H1(Optional.ofNullable(getAnswerText(currentCard)).orElse(""));

        cardLayout.add(question, divider, answer);

        addExampleIfPresent(currentCard, cardLayout);
        cardContent.add(cardLayout);
    }

    /**
     * Adds example text to the card layout if present.
     *
     * @param currentCard the current flashcard
     * @param cardLayout the card layout to add example to
     */
    private void addExampleIfPresent(final Flashcard currentCard, final VerticalLayout cardLayout) {
        if (currentCard.getExample() != null && !currentCard.getExample().isBlank()) {
            Span exampleText =
                    new Span(getTranslation(PracticeConstants.PRACTICE_EXAMPLE_PREFIX_KEY, currentCard.getExample()));
            cardLayout.add(exampleText);
        }
    }

    /**
     * Updates action buttons visibility for answer state.
     */
    private void updateActionButtonsForAnswer() {
        showAnswerButton.setVisible(false);
        knowButton.setVisible(true);
        hardButton.setVisible(true);
    }

    private void markLabeled(final String label) {
        if (!isValidSession()) {
            return;
        }

        processCardLabel(label);
        updateSessionStats();
        updateProgress();
        hideActionButtons();
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
            presenter.markKnow(session);
        } else {
            presenter.markHard(session);
        }
    }

    /**
     * Updates session statistics from the presenter.
     */
    private void updateSessionStats() {
        totalViewed = session.getTotalViewed();
        correctCount = session.getCorrectCount();
        hardCount = session.getHardCount();
    }

    /**
     * Hides the action buttons after marking.
     */
    private void hideActionButtons() {
        knowButton.setVisible(false);
        hardButton.setVisible(false);
    }

    private void nextCard() {
        if (presenter.isComplete(session)) {
            showPracticeComplete();
        } else {
            showCurrentCard();
        }
    }

    private void showSessionButtons() {
        actionButtons.removeAll();
        actionButtons.add(showAnswerButton, knowButton, hardButton);
        showAnswerButton.setVisible(true);
        knowButton.setVisible(false);
        hardButton.setVisible(false);
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
        cardContent.removeAll();
        VerticalLayout completionLayout = new VerticalLayout();
        completionLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        completionLayout.setSpacing(true);

        int total = calculateTotalCards();
        H1 completionTitle =
                new H1(getTranslation(PracticeConstants.PRACTICE_SESSION_COMPLETE_KEY, currentDeck.getTitle()));
        H3 results = new H3(getTranslation(PracticeConstants.PRACTICE_RESULTS_KEY, correctCount, total, hardCount));
        Span timeInfo = createTimeInfo();

        completionLayout.add(completionTitle, results, timeInfo);
        cardContent.add(completionLayout);
    }

    /**
     * Calculates the total number of cards in the session.
     *
     * @return total number of cards
     */
    private int calculateTotalCards() {
        return (session != null && session.getCards() != null)
                ? session.getCards().size()
                : totalViewed;
    }

    /**
     * Creates time information display for the completion screen.
     *
     * @return configured time info span
     */
    private Span createTimeInfo() {
        long minutes = java.time.Duration.between(session.getSessionStart(), java.time.Instant.now())
                .toMinutes();
        minutes = Math.clamp(minutes, 1, Integer.MAX_VALUE);
        double denom = Math.clamp(totalViewed, 1.0, Double.MAX_VALUE);
        long avgSec = Math.round((session.getTotalAnswerDelayMs() / denom) / 1000.0);
        return new Span(getTranslation(PracticeConstants.PRACTICE_TIME_KEY, minutes, avgSec));
    }

    /**
     * Creates completion action buttons for session end.
     */
    private void createCompletionButtons() {
        actionButtons.removeAll();
        Button againButton = createRepeatButton();
        Button backToDeckButton = createBackToDeckButton();
        Button homeButton = createHomeButton();
        actionButtons.add(againButton, backToDeckButton, homeButton);
    }

    /**
     * Creates the repeat practice button for failed cards.
     *
     * @return configured repeat button
     */
    private Button createRepeatButton() {
        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_REPEAT_HARD_KEY),
                e -> handleRepeatPractice(),
                ButtonVariant.LUMO_ERROR,
                ButtonVariant.LUMO_LARGE);
    }

    /**
     * Handles repeat practice for failed cards.
     */
    private void handleRepeatPractice() {
        List<Flashcard> failed = getFailedCards();
        showSessionButtons();
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
        session = new PracticePresenter.Session(currentDeck.getId(), new ArrayList<>(failedCards));
        Collections.shuffle(session.getCards());
        correctCount = 0;
        hardCount = 0;
        totalViewed = 0;
        showCurrentCard();
    }

    /**
     * Creates the back to deck button.
     *
     * @return configured back to deck button
     */
    private Button createBackToDeckButton() {
        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY),
                e -> NavigationHelper.navigateToDeck(currentDeck.getId()));
    }

    /**
     * Creates the home button.
     *
     * @return configured home button
     */
    private Button createHomeButton() {
        return ButtonHelper.createButton(
                getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECKS_KEY), e -> NavigationHelper.navigateToDecks());
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
