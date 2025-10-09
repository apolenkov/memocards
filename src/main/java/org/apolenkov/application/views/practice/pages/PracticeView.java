package org.apolenkov.application.views.practice.pages;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.exceptions.EntityNotFoundException;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.practice.business.PracticePresenter;
import org.apolenkov.application.views.practice.business.PracticeSession;
import org.apolenkov.application.views.practice.components.PracticeActions;
import org.apolenkov.application.views.practice.components.PracticeCard;
import org.apolenkov.application.views.practice.components.PracticeConstants;
import org.apolenkov.application.views.practice.components.PracticeHeader;
import org.apolenkov.application.views.practice.components.PracticeProgress;
import org.apolenkov.application.views.practice.controllers.PracticeCompletionFlow;
import org.apolenkov.application.views.practice.controllers.PracticeSessionFlow;
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
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    // Dependencies
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient PracticePresenter presenter;

    // Controllers
    private transient PracticeSessionFlow sessionFlow;
    private transient PracticeCompletionFlow completionFlow;

    // Data
    private transient Deck currentDeck;
    private transient PracticeSession session;
    private transient PracticeDirection sessionDirection;

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
        initializeControllers();

        // Add components to layout first - this triggers initContent() for Composite components
        pageSection.add(practiceHeader, practiceProgress, practiceCard, practiceActions);

        // Setup handlers AFTER components are added and initialized
        setupActionHandlers();

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

        // Load practice direction from user settings
        sessionDirection = presenter.defaultDirection();
    }

    /**
     * Initializes controllers with components.
     */
    private void initializeControllers() {
        sessionFlow = new PracticeSessionFlow(presenter, practiceCard, practiceProgress, practiceActions);
        completionFlow = new PracticeCompletionFlow(presenter, flashcardUseCase, practiceCard, practiceActions);
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
            LOGGER.debug("Initializing practice session for deck ID: {}", parameter);

            long deckId = parseDeckId(parameter);
            loadDeck(deckId);
            if (currentDeck != null) {
                AUDIT_LOGGER.info(
                        "User started practice session for deck '{}' (ID: {})",
                        currentDeck.getTitle(),
                        currentDeck.getId());
                startDefaultPractice();
                LOGGER.debug("Practice session initialized successfully for deck: {}", currentDeck.getTitle());
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid deck ID format: {}", parameter, e);
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

    /**
     * Starts default practice session.
     */
    private void startDefaultPractice() {
        session = sessionFlow.startDefaultPractice(currentDeck, sessionDirection);
        if (session == null) {
            showAllKnownDialogAndRedirect();
        }
    }

    /**
     * Shows congratulations dialog and redirects to deck when all cards are already studied.
     */
    private void showAllKnownDialogAndRedirect() {
        // Hide only the practice components, keep the header visible
        if (practiceProgress != null) {
            practiceProgress.setVisible(false);
        }
        if (practiceActions != null) {
            practiceActions.setVisible(false);
        }

        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setModal(true);
        dialog.setDraggable(false);
        dialog.setResizable(false);

        H3 title = new H3(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_TITLE_KEY));
        Paragraph message =
                new Paragraph(getTranslation(PracticeConstants.PRACTICE_ALL_KNOWN_MESSAGE_KEY, currentDeck.getTitle()));

        Button backToDeckButton = new Button(getTranslation(PracticeConstants.PRACTICE_BACK_TO_DECK_KEY), e -> {
            dialog.close();
            NavigationHelper.navigateToDeck(currentDeck.getId());
        });
        backToDeckButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        backToDeckButton.focus();

        VerticalLayout dialogLayout = new VerticalLayout(title, message, backToDeckButton);
        dialogLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        dialog.add(dialogLayout);
        dialog.open();

        LOGGER.debug("All cards known for deck '{}', showing dialog and will redirect to deck", currentDeck.getTitle());
    }

    /**
     * Loads the deck by ID.
     *
     * @param deckId the deck ID to load
     */
    private void loadDeck(final long deckId) {
        Optional<Deck> deckOpt = presenter.loadDeck(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            practiceHeader.setDeckTitle(getTranslation(PracticeConstants.PRACTICE_TITLE_KEY, currentDeck.getTitle()));
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
     * Shows the answer for the current card.
     */
    private void showAnswer() {
        session = sessionFlow.showAnswer(session, sessionDirection);
    }

    /**
     * Marks the current card with the specified label.
     *
     * @param label the label to apply (know or hard)
     */
    private void markLabeled(final String label) {
        session = sessionFlow.markLabeled(session, label, sessionDirection);
        if (presenter.isComplete(session)) {
            handlePracticeComplete();
        }
    }

    /**
     * Handles practice session completion.
     */
    private void handlePracticeComplete() {
        AUDIT_LOGGER.info(
                "User completed practice session for deck '{}' (ID: {})", currentDeck.getTitle(), currentDeck.getId());
        session = completionFlow.showPracticeComplete(session, currentDeck, this::handleRepeatSession);
    }

    /**
     * Handles repeat practice with new session.
     *
     * @param newSession the new session with failed cards
     */
    private void handleRepeatSession(final PracticeSession newSession) {
        session = newSession;
        sessionFlow.showCurrentCard(session, sessionDirection);
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
