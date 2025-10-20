package org.apolenkov.application.views.deck.pages;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import java.util.Optional;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.config.ui.UIConfig;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.StatsService;
import org.apolenkov.application.views.core.exception.EntityNotFoundException;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.deck.components.DeckDetailHeader;
import org.apolenkov.application.views.deck.components.detail.DeckFlashcardContainer;
import org.apolenkov.application.views.deck.components.dialogs.DeckDeleteDialog;
import org.apolenkov.application.views.deck.components.dialogs.DeckEditDialog;
import org.apolenkov.application.views.deck.components.dialogs.DeckFlashcardDeleteDialog;
import org.apolenkov.application.views.deck.components.dialogs.DeckFlashcardDialog;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = RouteConstants.DECK_ROUTE, layout = PublicLayout.class)
@RolesAllowed(SecurityConstants.ROLE_USER)
public class DeckView extends Composite<VerticalLayout>
        implements HasUrlParameter<String>, HasDynamicTitle, AfterNavigationObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckView.class);

    // ==================== Fields ====================

    // Dependencies
    private final transient DeckUseCase deckUseCase;
    private final transient FlashcardUseCase flashcardUseCase;
    private final transient StatsService statsService;
    private final transient UIConfig uiConfig;

    // State
    private transient Deck currentDeck;

    // UI Components
    private DeckDetailHeader detailHeader;
    private DeckFlashcardContainer flashcardContainer;

    // Event Registrations
    private Registration practiceClickListenerRegistration;
    private Registration resetProgressClickListenerRegistration;
    private Registration addFlashcardClickListenerRegistration;
    private Registration editDeckClickListenerRegistration;
    private Registration deleteDeckClickListenerRegistration;
    private Registration filterChangeListenerRegistration;

    // ==================== Constructor ====================

    /**
     * Creates a new DeckView with required dependencies.
     *
     * @param deckUseCaseParam use case for deck operations
     * @param flashcardUseCaseParam use case for flashcard operations
     * @param statsServiceParam service for statistics tracking
     * @param uiConfigParam UI configuration settings
     */
    public DeckView(
            final DeckUseCase deckUseCaseParam,
            final FlashcardUseCase flashcardUseCaseParam,
            final StatsService statsServiceParam,
            final UIConfig uiConfigParam) {
        this.deckUseCase = deckUseCaseParam;
        this.flashcardUseCase = flashcardUseCaseParam;
        this.statsService = statsServiceParam;
        this.uiConfig = uiConfigParam;
    }

    // ==================== Lifecycle Methods ====================

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    public void init() {
        // Configure main view layout like DecksView
        getContent().setPadding(false);
        getContent().setSpacing(false);
        getContent().setWidthFull();

        // Create content layout with same structure as DecksView
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.addClassName(DeckConstants.DECKS_VIEW_CONTENT_CLASS);
        getContent().add(content);

        // Show loading state initially
        showLoadingState(content);
    }

    /**
     * Gets the page title for the deck view.
     *
     * @return the localized deck cards title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(DeckConstants.DECK_CARDS_KEY);
    }

    /**
     * Called after navigation to this view is complete.
     * Sets up event listeners and updates deck information.
     * This method is called ONCE per navigation - no flag needed.
     *
     * @param event the after navigation event
     */
    @Override
    public void afterNavigation(final AfterNavigationEvent event) {
        // Setup event listeners for deck actions
        setupActionListeners();
        // Update deck info after navigation is complete
        updateDeckInfo();
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);

        if (practiceClickListenerRegistration != null) {
            practiceClickListenerRegistration.remove();
            practiceClickListenerRegistration = null;
        }

        if (resetProgressClickListenerRegistration != null) {
            resetProgressClickListenerRegistration.remove();
            resetProgressClickListenerRegistration = null;
        }

        if (addFlashcardClickListenerRegistration != null) {
            addFlashcardClickListenerRegistration.remove();
            addFlashcardClickListenerRegistration = null;
        }

        if (editDeckClickListenerRegistration != null) {
            editDeckClickListenerRegistration.remove();
            editDeckClickListenerRegistration = null;
        }

        if (deleteDeckClickListenerRegistration != null) {
            deleteDeckClickListenerRegistration.remove();
            deleteDeckClickListenerRegistration = null;
        }

        if (filterChangeListenerRegistration != null) {
            filterChangeListenerRegistration.remove();
            filterChangeListenerRegistration = null;
        }
    }

    // ==================== Setup Methods ====================

    /**
     * Sets up event listeners for deck action buttons.
     * Configures click handlers for practice, reset, add, edit and delete actions.
     */
    private void setupActionListeners() {
        if (detailHeader == null || flashcardContainer == null) {
            return;
        }

        setupPracticeButtonListener();
        setupResetProgressButtonListener();
        setupAddFlashcardButtonListener();
        setupEditDeckButtonListener();
        setupDeleteDeckButtonListener();
        setupFilterChangeListener();
    }

    /**
     * Sets up the practice button click listener.
     */
    private void setupPracticeButtonListener() {
        if (practiceClickListenerRegistration == null) {
            practiceClickListenerRegistration = detailHeader.addPracticeClickListener(e -> {
                if (currentDeck != null) {
                    NavigationHelper.navigateToPractice(currentDeck.getId());
                }
            });
        }
    }

    /**
     * Sets up the reset progress button click listener with confirmation dialog.
     */
    private void setupResetProgressButtonListener() {
        if (resetProgressClickListenerRegistration == null) {
            resetProgressClickListenerRegistration =
                    detailHeader.addResetProgressClickListener(e -> showResetConfirmationDialog());
        }
    }

    /**
     * Sets up the add flashcard button click listener from header actions.
     */
    private void setupAddFlashcardButtonListener() {
        if (addFlashcardClickListenerRegistration == null) {
            addFlashcardClickListenerRegistration =
                    detailHeader.addAddCardClickListener(e -> openFlashcardDialog(null));
        }
    }

    /**
     * Sets up the edit deck button click listener.
     */
    private void setupEditDeckButtonListener() {
        if (editDeckClickListenerRegistration == null) {
            editDeckClickListenerRegistration = detailHeader.addEditDeckClickListener(e -> {
                if (currentDeck != null) {
                    // Cache invalidation handled automatically via DeckModifiedEvent
                    // published by DeckUseCaseService after save/delete operations
                    new DeckEditDialog(deckUseCase, currentDeck, updated -> updateDeckInfo()).open();
                }
            });
        }
    }

    /**
     * Sets up the delete deck button click listener.
     */
    private void setupDeleteDeckButtonListener() {
        if (deleteDeckClickListenerRegistration == null) {
            deleteDeckClickListenerRegistration = detailHeader.addDeleteDeckClickListener(e -> deleteDeck());
        }
    }

    /**
     * Sets up the filter change listener from toolbar.
     */
    private void setupFilterChangeListener() {
        if (filterChangeListenerRegistration == null) {
            filterChangeListenerRegistration = flashcardContainer.addFilterChangeListener(
                    filterOption -> LOGGER.debug("Filter changed to: {}", filterOption));
        }
    }

    /**
     * Sets the deck ID parameter from the URL and loads the deck.
     *
     * @param event the navigation event containing URL parameters
     * @param parameter the deck ID as a string from the URL
     */
    @Override
    public void setParameter(final BeforeEvent event, final String parameter) {
        try {
            long deckId = Long.parseLong(parameter);
            loadDeck(deckId);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid deck ID parameter: {}", parameter);
            // Throw exception for invalid ID - will be caught by EntityNotFoundErrorHandler
            throw new EntityNotFoundException(
                    parameter, RouteConstants.DECKS_ROUTE, getTranslation(DeckConstants.DECK_INVALID_ID_KEY));
        }
    }

    // ==================== UI State Management ====================

    /**
     * Shows loading state while deck is being loaded.
     *
     * @param contentContainer the container where loading state will be displayed
     */
    private void showLoadingState(final VerticalLayout contentContainer) {
        contentContainer.removeAll();

        VerticalLayout loadingContainer = new VerticalLayout();
        loadingContainer.setSpacing(true);
        loadingContainer.setWidthFull();
        loadingContainer.addClassName(DeckConstants.CONTAINER_MD_CLASS);
        loadingContainer.addClassName(DeckConstants.DECKS_SECTION_CLASS);
        loadingContainer.addClassName(DeckConstants.SURFACE_PANEL_CLASS);
        loadingContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        VerticalLayout loadingSection = new VerticalLayout();
        loadingSection.setSpacing(true);
        loadingSection.setPadding(true);
        loadingSection.setWidthFull();
        loadingSection.addClassName(DeckConstants.DECK_VIEW_SECTION_CLASS);
        loadingSection.addClassName(DeckConstants.SURFACE_PANEL_CLASS);
        loadingSection.addClassName(DeckConstants.CONTAINER_MD_CLASS);

        H2 loadingTitle = new H2(getTranslation(DeckConstants.DECK_LOADING_STATE));
        loadingTitle.addClassName(DeckConstants.DECK_VIEW_TITLE_CLASS);

        loadingSection.add(loadingTitle);
        loadingContainer.add(loadingSection);
        contentContainer.add(loadingContainer);
    }

    // ==================== Content Creation ====================

    /**
     * Creates the main deck content after successful deck loading.
     */
    private void createDeckContent() {
        // Get the content container from init()
        VerticalLayout contentContainer =
                (VerticalLayout) getContent().getChildren().findFirst().orElse(null);
        if (contentContainer == null) {
            return;
        }

        contentContainer.removeAll();

        // Main content container with same structure as DeckContainer
        VerticalLayout deckContainer = new VerticalLayout();
        deckContainer.setSpacing(true);
        deckContainer.setAlignItems(FlexComponent.Alignment.CENTER);
        deckContainer.setWidthFull();
        deckContainer.addClassName(DeckConstants.CONTAINER_MD_CLASS);
        deckContainer.addClassName(DeckConstants.DECKS_SECTION_CLASS);
        deckContainer.addClassName(DeckConstants.SURFACE_PANEL_CLASS);

        // Create components
        detailHeader = new DeckDetailHeader();
        flashcardContainer = new DeckFlashcardContainer(
                statsService,
                flashcardUseCase,
                uiConfig.search().debounceMs(),
                uiConfig.pagination().pageSize());

        // Set callbacks
        flashcardContainer.setEditFlashcardCallback(this::openFlashcardDialog);
        flashcardContainer.setDeleteFlashcardCallback(this::deleteFlashcard);

        // Add components to container
        deckContainer.add(detailHeader, flashcardContainer);
        contentContainer.add(deckContainer);
    }

    // ==================== Data Loading ====================

    /**
     * Loads a deck by ID and initializes the view.
     *
     * @param deckId the ID of the deck to load
     * @throws EntityNotFoundException if deck is not found
     */
    private void loadDeck(final long deckId) {
        Optional<Deck> deckOpt = deckUseCase.getDeckById(deckId);
        if (deckOpt.isPresent()) {
            currentDeck = deckOpt.get();
            LOGGER.info("Deck loaded successfully: {}", currentDeck.getTitle());
        } else {
            LOGGER.warn("Deck not found with ID: {}", deckId);
            throw new EntityNotFoundException(
                    String.valueOf(deckId), RouteConstants.DECKS_ROUTE, getTranslation(DeckConstants.DECK_NOT_FOUND));
        }

        createDeckContent();
        updateDeckInfo();

        // Initialize data provider with current deck ID
        if (currentDeck != null && flashcardContainer != null) {
            flashcardContainer.setCurrentDeckId(currentDeck.getId());
        }
    }

    /**
     * Updates the display of deck information (title, stats, description).
     */
    private void updateDeckInfo() {
        if (currentDeck != null && detailHeader != null) {
            detailHeader.setDeckTitle(currentDeck.getTitle());
            // New format: "(description) X карточки"
            String description = Optional.ofNullable(currentDeck.getDescription())
                    .filter(desc -> !desc.trim().isEmpty())
                    .orElse("");

            long count = flashcardUseCase.countByDeckId(currentDeck.getId());
            String statsText = description.isEmpty()
                    ? getTranslation(DeckConstants.DECK_COUNT, count)
                    : String.format("(%s) %s", description, getTranslation(DeckConstants.DECK_COUNT_SHORT, count));

            detailHeader.setDeckStats(statsText);
        }
    }

    // ==================== Dialog Handlers ====================

    /**
     * Opens a dialog for creating or editing a flashcard.
     *
     * @param flashcard the flashcard to edit, or null for creating new
     */
    private void openFlashcardDialog(final Flashcard flashcard) {
        DeckFlashcardDialog dialog = new DeckFlashcardDialog(flashcardUseCase, currentDeck, savedFlashcard -> {
            // Refresh grid data to reflect changes (lazy loading handles updates)
            if (flashcardContainer != null) {
                flashcardContainer.refreshData();
            }
            updateDeckInfo();
        });

        if (flashcard == null) {
            dialog.openForCreate();
        } else {
            dialog.openForEdit(flashcard);
        }
    }

    /**
     * Deletes a flashcard with confirmation dialog.
     *
     * @param flashcard the flashcard to delete
     */
    private void deleteFlashcard(final Flashcard flashcard) {
        DeckFlashcardDeleteDialog dialog =
                new DeckFlashcardDeleteDialog(flashcardUseCase, flashcard, deletedFlashcardId -> {
                    // Refresh grid data to reflect deletion (lazy loading handles updates)
                    if (flashcardContainer != null) {
                        flashcardContainer.refreshData();
                    }
                    updateDeckInfo();
                    LOGGER.info("Flashcard {} deleted successfully", deletedFlashcardId);
                });

        UI.getCurrent().add(dialog);
        dialog.show();
    }

    /**
     * Initiates deck deletion process with appropriate confirmation dialog.
     * Shows simple dialog for empty decks, complex dialog for decks with cards.
     */
    private void deleteDeck() {
        if (currentDeck == null) {
            return;
        }

        DeckDeleteDialog dialog = new DeckDeleteDialog(
                deckUseCase,
                flashcardUseCase,
                currentDeck,
                deletedDeck -> LOGGER.info("Deck {} deleted successfully", currentDeck.getId()));

        dialog.show();
    }

    /**
     * Shows confirmation dialog before resetting progress.
     */
    private void showResetConfirmationDialog() {
        com.vaadin.flow.component.confirmdialog.ConfirmDialog dialog =
                new com.vaadin.flow.component.confirmdialog.ConfirmDialog();

        dialog.setHeader(getTranslation("deck.reset.confirm.title"));
        dialog.setText(getTranslation("deck.reset.confirm.message"));

        dialog.setCancelable(true);
        dialog.setConfirmText(getTranslation("common.yes"));
        dialog.setCancelText(getTranslation("common.cancel"));
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(event -> {
            LOGGER.info("Reset progress confirmed by user");
            if (flashcardContainer != null) {
                flashcardContainer.resetProgress();
                org.apolenkov.application.views.shared.utils.NotificationHelper.showSuccessBottom(
                        getTranslation(DeckConstants.DECK_PROGRESS_RESET));
            }
        });

        dialog.addCancelListener(event -> LOGGER.debug("Reset progress cancelled by user"));

        dialog.open();
    }
}
