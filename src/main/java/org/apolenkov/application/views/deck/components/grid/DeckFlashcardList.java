package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.StatsService;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.springframework.data.domain.PageRequest;

/**
 * Component for displaying flashcards with explicit pagination controls.
 * Uses page-based loading to efficiently handle large collections (500+ cards).
 * Each flashcard is rendered as a card with front text, example, status, and actions.
 */
public final class DeckFlashcardList extends VerticalLayout {

    // Dependencies
    private final transient StatsService statsService;
    private final transient FlashcardUseCase flashcardUseCase;

    // Configuration
    private final int pageSize;

    // Refresh debouncing to prevent triple refresh
    private final AtomicBoolean refreshPending = new AtomicBoolean(false);
    private transient Registration refreshTimer;

    // UI Components
    private Div cardsContainer;
    private Span topPaginationInfo;
    private Span bottomPaginationInfo;
    private Button topPrevButton;
    private Button topNextButton;
    private Span topPageInfo;
    private Button bottomPrevButton;
    private Button bottomNextButton;
    private Span bottomPageInfo;

    // Callbacks (stored for rendering)
    private transient Consumer<Flashcard> editFlashcardCallback;
    private transient Consumer<Flashcard> deleteFlashcardCallback;
    private transient Consumer<Flashcard> toggleKnownCallback;

    // State
    private transient Long currentDeckId;
    private transient FlashcardFilter currentFilter;
    private int currentPage = 0;
    private int totalPages = 0;
    private long totalItems = 0;

    // Lifecycle
    private boolean hasBeenInitialized = false;

    /**
     * Creates a new DeckFlashcardList component with lazy loading.
     *
     * @param statsServiceParam service for statistics tracking
     * @param flashcardUseCaseParam use case for flashcard operations
     * @param pageSizeParam number of items per page
     */
    public DeckFlashcardList(
            final StatsService statsServiceParam,
            final FlashcardUseCase flashcardUseCaseParam,
            final int pageSizeParam) {
        this.statsService = statsServiceParam;
        this.flashcardUseCase = flashcardUseCaseParam;
        this.pageSize = pageSizeParam;
        this.currentFilter = new FlashcardFilter(null, FilterOption.UNKNOWN_ONLY);

        setWidthFull();
        setPadding(false);
        setSpacing(true);
        addClassName("deck-flashcard-list");
    }

    /**
     * Initializes the component when attached to the UI.
     * Creates card container with explicit pagination controls.
     *
     * @param attachEvent the attachment event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (!hasBeenInitialized) {
            hasBeenInitialized = true;
            initializeLayout();
        }
    }

    /**
     * Initializes the layout with pagination controls and cards container.
     */
    private void initializeLayout() {
        // Top pagination info
        topPaginationInfo = new Span();
        topPaginationInfo.addClassName("deck-pagination-info");

        // Top pagination controls
        HorizontalLayout topPagination = new HorizontalLayout();
        topPagination.setWidthFull();
        topPagination.setJustifyContentMode(JustifyContentMode.CENTER);
        topPagination.setAlignItems(Alignment.CENTER);
        topPagination.setSpacing(true);
        topPagination.setPadding(true);
        topPagination.addClassName("deck-pagination-top");

        topPrevButton = new Button(getTranslation("deck.pagination.previous"), VaadinIcon.ANGLE_LEFT.create());
        topPrevButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        topPrevButton.addClickListener(e -> goToPreviousPage());

        topPageInfo = new Span();
        topPageInfo.addClassName("pagination-page-info");
        topPageInfo.getStyle().set("margin", "0 var(--lumo-space-m)");

        topNextButton = new Button(getTranslation("deck.pagination.next"), VaadinIcon.ANGLE_RIGHT.create());
        topNextButton.setIconAfterText(true);
        topNextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        topNextButton.addClickListener(e -> goToNextPage());

        topPagination.add(topPrevButton, topPageInfo, topNextButton);

        // Cards container
        cardsContainer = new Div();
        cardsContainer.addClassName("deck-flashcard-list");
        cardsContainer.setWidthFull();

        // Bottom pagination controls
        HorizontalLayout bottomPagination = new HorizontalLayout();
        bottomPagination.setWidthFull();
        bottomPagination.setJustifyContentMode(JustifyContentMode.CENTER);
        bottomPagination.setAlignItems(Alignment.CENTER);
        bottomPagination.setSpacing(true);
        bottomPagination.setPadding(true);
        bottomPagination.addClassName("deck-pagination-bottom");

        bottomPrevButton = new Button(getTranslation("deck.pagination.previous"), VaadinIcon.ANGLE_LEFT.create());
        bottomPrevButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        bottomPrevButton.addClickListener(e -> goToPreviousPage());

        bottomPageInfo = new Span();
        bottomPageInfo.addClassName("pagination-page-info");
        bottomPageInfo.getStyle().set("margin", "0 var(--lumo-space-m)");

        bottomNextButton = new Button(getTranslation("deck.pagination.next"), VaadinIcon.ANGLE_RIGHT.create());
        bottomNextButton.setIconAfterText(true);
        bottomNextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        bottomNextButton.addClickListener(e -> goToNextPage());

        bottomPagination.add(bottomPrevButton, bottomPageInfo, bottomNextButton);

        // Bottom pagination info
        bottomPaginationInfo = new Span();
        bottomPaginationInfo.addClassName("deck-pagination-info");

        // Add all components
        add(topPaginationInfo, topPagination, cardsContainer, bottomPagination, bottomPaginationInfo);

        // Load data if deckId is already set
        if (currentDeckId != null) {
            loadCurrentPage();
        }
    }

    /**
     * Navigates to the previous page.
     */
    private void goToPreviousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadCurrentPage();
        }
    }

    /**
     * Navigates to the next page.
     */
    private void goToNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadCurrentPage();
        }
    }

    /**
     * Sets the current deck ID and loads first page.
     *
     * @param deckId the deck ID
     */
    public void setCurrentDeckId(final Long deckId) {
        this.currentDeckId = deckId;
        this.currentPage = 0;
        // Load data if container is already initialized
        if (cardsContainer != null) {
            loadCurrentPage();
        }
        // If cardsContainer is null, onAttach will call loadCurrentPage later
    }

    /**
     * Loads the current page of flashcards from the database.
     * If current page is empty but items exist, automatically navigates to the last valid page.
     */
    private void loadCurrentPage() {
        if (currentDeckId == null || cardsContainer == null) {
            return;
        }

        // Calculate total items and pages based on current filter
        calculatePagination();

        // Load flashcards for current page
        PageRequest pageRequest = PageRequest.of(currentPage, pageSize);
        List<Flashcard> flashcards = loadFlashcardsForPage(pageRequest);

        // If current page is empty but we have items (page became empty after deletion),
        // fall back to last valid page
        // Note: This should rarely happen since updateFilter() resets to page 0
        if (flashcards.isEmpty() && totalItems > 0 && currentPage > 0) {
            currentPage = Math.max(0, totalPages - 1);
            calculatePagination();
            pageRequest = PageRequest.of(currentPage, pageSize);
            flashcards = loadFlashcardsForPage(pageRequest);
        }

        // Load known card IDs once for all flashcards (prevents N+50 cache hits)
        Set<Long> knownCardIds = currentDeckId != null ? statsService.getKnownCardIds(currentDeckId) : Set.of();

        // Clear existing cards and render new ones
        cardsContainer.removeAll();
        flashcards.forEach(flashcard -> {
            Div cardDiv = createFlashcardCard(flashcard, knownCardIds);
            cardsContainer.add(cardDiv);
        });

        // Update pagination info
        updatePaginationInfo();
    }

    /**
     * Calculates total items and pages based on current filter.
     * Handles combinations of search query and known/unknown status.
     */
    private void calculatePagination() {
        boolean hasSearch = currentFilter != null
                && currentFilter.searchQuery() != null
                && !currentFilter.searchQuery().trim().isEmpty();

        FilterOption filterOption = currentFilter != null ? currentFilter.filterOption() : FilterOption.ALL;

        // ✅ Use dynamic SQL query builder - ONE method handles ALL combinations!
        String searchQuery = hasSearch ? currentFilter.searchQuery() : null;
        totalItems = flashcardUseCase.countFlashcardsWithFilter(currentDeckId, searchQuery, filterOption);

        totalPages = (int) Math.ceil((double) totalItems / pageSize);

        // Ensure current page is valid
        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
    }

    /**
     * Loads flashcards for the specified page with current filter applied.
     * Handles combinations of search query and known/unknown status filtering.
     *
     * @param pageRequest the page request
     * @return list of flashcards matching current filter
     */
    private List<Flashcard> loadFlashcardsForPage(final PageRequest pageRequest) {
        boolean hasSearch = currentFilter != null
                && currentFilter.searchQuery() != null
                && !currentFilter.searchQuery().trim().isEmpty();

        FilterOption filterOption = currentFilter != null ? currentFilter.filterOption() : FilterOption.ALL;

        // ✅ Use dynamic SQL query builder - ONE method handles ALL combinations!
        String searchQuery = hasSearch ? currentFilter.searchQuery() : null;
        return flashcardUseCase.getFlashcardsWithFilter(currentDeckId, searchQuery, filterOption, pageRequest);
    }

    /**
     * Gets contextual empty message based on current filter and search state.
     * Provides helpful guidance to users about why no items are shown.
     *
     * @return localized message explaining the empty state
     */
    private String getContextualEmptyMessage() {
        // Check if there's an active search query
        boolean hasSearch = currentFilter != null
                && currentFilter.searchQuery() != null
                && !currentFilter.searchQuery().trim().isEmpty();

        // If searching, show generic "no results" message
        if (hasSearch) {
            return getTranslation("deck.pagination.no-items");
        }

        // Otherwise, show contextual message based on filter
        FilterOption filterOption = currentFilter != null ? currentFilter.filterOption() : FilterOption.ALL;
        return switch (filterOption) {
            case ALL -> getTranslation("deck.pagination.no-items.all");
            case KNOWN_ONLY -> getTranslation("deck.pagination.no-items.known");
            case UNKNOWN_ONLY -> getTranslation("deck.pagination.no-items.unknown");
        };
    }

    /**
     * Updates pagination info displays.
     */
    private void updatePaginationInfo() {
        if (totalItems == 0) {
            String noItemsText = getContextualEmptyMessage();
            topPaginationInfo.setText(noItemsText);

            // Hide pagination controls when no items
            hidePaginationControls();
            return;
        }

        // Show pagination controls when items exist
        showPaginationControls();

        int startItem = currentPage * pageSize + 1;
        int endItem = Math.min((currentPage + 1) * pageSize, (int) totalItems);

        // Use mobile-friendly text for all screens (CSS will handle responsive display)
        String paginationText = getTranslation(
                "deck.pagination.info.mobile",
                startItem,
                endItem,
                totalItems,
                currentPage + 1,
                Math.max(1, totalPages));

        topPaginationInfo.setText(paginationText);
        bottomPaginationInfo.setText(paginationText);

        // Add data attribute for mobile CSS to show only page numbers
        String pageInfoText = (currentPage + 1) + " / " + Math.max(1, totalPages);
        topPaginationInfo.getElement().setAttribute("data-page-info", pageInfoText);
        bottomPaginationInfo.getElement().setAttribute("data-page-info", pageInfoText);
    }

    /**
     * Updates pagination button states.
     *
     * @param canGoPrevious whether previous button should be enabled
     * @param canGoNext whether next button should be enabled
     */
    private void updatePaginationButtons(final boolean canGoPrevious, final boolean canGoNext) {
        if (topPrevButton != null) {
            topPrevButton.setEnabled(canGoPrevious);
        }
        if (topNextButton != null) {
            topNextButton.setEnabled(canGoNext);
        }
        if (bottomPrevButton != null) {
            bottomPrevButton.setEnabled(canGoPrevious);
        }
        if (bottomNextButton != null) {
            bottomNextButton.setEnabled(canGoNext);
        }
    }

    /**
     * Updates page info labels in pagination controls.
     *
     * @param text the page info text
     */
    private void updatePageInfoLabels(final String text) {
        if (topPageInfo != null) {
            topPageInfo.setText(text);
        }
        if (bottomPageInfo != null) {
            bottomPageInfo.setText(text);
        }
    }

    /**
     * Hides pagination controls when no items are available.
     */
    private void hidePaginationControls() {
        // Hide top pagination
        if (getComponentCount() > 1) {
            Component topPagination = getComponentAt(1);
            if (topPagination != null) {
                topPagination.setVisible(false);
            }
        }

        // Hide bottom pagination
        if (getComponentCount() > 3) {
            Component bottomPagination = getComponentAt(3);
            if (bottomPagination != null) {
                bottomPagination.setVisible(false);
            }
        }
    }

    /**
     * Shows pagination controls when items are available.
     */
    private void showPaginationControls() {
        // Show top pagination
        if (getComponentCount() > 1) {
            Component topPagination = getComponentAt(1);
            if (topPagination != null) {
                topPagination.setVisible(true);
            }
        }

        // Show bottom pagination
        if (getComponentCount() > 3) {
            Component bottomPagination = getComponentAt(3);
            if (bottomPagination != null) {
                bottomPagination.setVisible(true);
            }
        }

        // Update button states and page info
        updatePaginationButtons(currentPage > 0, currentPage < totalPages - 1);

        String pageInfoText = (currentPage + 1) + " / " + Math.max(1, totalPages);
        updatePageInfoLabels(pageInfoText);
    }

    /**
     * Sets the edit flashcard callback.
     *
     * @param callback the callback to execute when editing a flashcard
     */
    public void setEditFlashcardCallback(final Consumer<Flashcard> callback) {
        this.editFlashcardCallback = callback;
    }

    /**
     * Sets the delete flashcard callback.
     *
     * @param callback the callback to execute when deleting a flashcard
     */
    public void setDeleteFlashcardCallback(final Consumer<Flashcard> callback) {
        this.deleteFlashcardCallback = callback;
    }

    /**
     * Sets the toggle known callback.
     *
     * @param callback the callback to execute when toggling known status
     */
    public void setToggleKnownCallback(final Consumer<Flashcard> callback) {
        this.toggleKnownCallback = callback;
    }

    /**
     * Updates the filter and reloads from first page.
     * Resets to page 0 to avoid edge cases where current page becomes invalid after filtering.
     *
     * @param searchQuery search query (can be null or empty)
     * @param filterOption filter option for known/unknown status
     */
    public void updateFilter(final String searchQuery, final FilterOption filterOption) {
        this.currentFilter = new FlashcardFilter(searchQuery, filterOption);
        // Reset to first page to avoid retry loop when filter reduces total items
        this.currentPage = 0;
        loadCurrentPage();
    }

    /**
     * Creates a card component for a single flashcard.
     * Called for each visible item in the current page.
     *
     * @param flashcard the flashcard to create a card for
     * @param knownCardIds set of known card IDs (pre-loaded to avoid N cache hits)
     * @return the card component
     */
    private Div createFlashcardCard(final Flashcard flashcard, final Set<Long> knownCardIds) {
        Div card = new Div();
        card.addClassName("flashcard-card");

        // Check known status from pre-loaded set (prevents N+50 cache hits per render)
        boolean isKnown = knownCardIds.contains(flashcard.getId());

        // Note: Filter logic is handled in DataProvider to avoid breaking VirtualList scroll
        // Hiding items in the renderer breaks VirtualList's virtual scrolling

        // Add known/unknown class for styling (green/blue left border)
        if (isKnown) {
            card.addClassName("flashcard-card--known");
        } else {
            card.addClassName("flashcard-card--unknown");
        }

        card.getStyle().set("width", "100%");
        card.getStyle().set("max-width", "100%");
        card.getStyle().set("cursor", "pointer"); // Indicate double-clickable

        // Add double-click listener to open edit dialog
        card.getElement().addEventListener("dblclick", e -> {
            if (editFlashcardCallback != null) {
                editFlashcardCallback.accept(flashcard);
            }
        });

        // Card content (clickable area)
        VerticalLayout cardContent = new VerticalLayout();
        cardContent.setPadding(true);
        cardContent.setSpacing(true);
        cardContent.setWidthFull();

        // Header row: Front text + Known indicator + Actions
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        headerRow.setJustifyContentMode(
                com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.BETWEEN);

        // Front text (grows to fill space)
        Span frontText = new Span(flashcard.getFrontText());
        frontText.addClassName("flashcard-front");
        frontText.getStyle().set("flex-grow", "1");

        // Right section: Known indicator + Actions
        HorizontalLayout rightSection = new HorizontalLayout();
        rightSection.setSpacing(true);
        rightSection.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        // Known indicator (checkmark if card is known) - hidden via CSS, replaced with border
        if (isKnown) {
            Span knownIndicator = new Span("✓");
            knownIndicator.addClassName("flashcard-known-indicator");
            rightSection.add(knownIndicator);
        }

        // Desktop: action buttons (visible only on desktop via CSS)
        HorizontalLayout desktopActions = createDesktopActionButtons(flashcard, isKnown);
        desktopActions.addClassName("desktop-only");

        // Mobile: same action buttons (visible only on mobile via CSS)
        HorizontalLayout mobileActions = createDesktopActionButtons(flashcard, isKnown);
        mobileActions.addClassName("mobile-only");

        rightSection.add(desktopActions, mobileActions);
        headerRow.add(frontText, rightSection);

        // Example row (if exists)
        if (flashcard.getExample() != null && !flashcard.getExample().trim().isEmpty()) {
            Span exampleText = new Span(flashcard.getExample());
            exampleText.addClassName("flashcard-example");
            cardContent.add(headerRow, exampleText);
        } else {
            cardContent.add(headerRow);
        }

        card.add(cardContent);
        return card;
    }

    /**
     * Creates desktop action buttons (icon buttons without text).
     *
     * @param flashcard the flashcard to create actions for
     * @param isKnown whether the flashcard is known (affects button styling)
     * @return the actions buttons layout
     */
    private HorizontalLayout createDesktopActionButtons(final Flashcard flashcard, final boolean isKnown) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(false);
        buttonsLayout.setPadding(false);
        buttonsLayout.addClassName("flashcard-desktop-actions");

        // Edit button
        com.vaadin.flow.component.button.Button editButton = new com.vaadin.flow.component.button.Button();
        editButton.setIcon(com.vaadin.flow.component.icon.VaadinIcon.EDIT.create());
        editButton.addThemeVariants(
                com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ICON);
        editButton
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, getTranslation(DeckConstants.FLASHCARD_MENU_EDIT));
        editButton.addClickListener(e -> {
            if (editFlashcardCallback != null) {
                editFlashcardCallback.accept(flashcard);
            }
        });

        // Toggle known button
        com.vaadin.flow.component.button.Button toggleButton = new com.vaadin.flow.component.button.Button();
        toggleButton.setIcon(com.vaadin.flow.component.icon.VaadinIcon.CHECK.create());
        toggleButton.addThemeVariants(
                com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ICON);

        // Green color for known cards, default for unknown
        if (isKnown) {
            toggleButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SUCCESS);
        }

        toggleButton
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, getTranslation(DeckConstants.FLASHCARD_MENU_TOGGLE));
        toggleButton.addClickListener(e -> {
            if (toggleKnownCallback != null) {
                toggleKnownCallback.accept(flashcard);
            }
        });

        // Delete button
        com.vaadin.flow.component.button.Button deleteButton = new com.vaadin.flow.component.button.Button();
        deleteButton.setIcon(com.vaadin.flow.component.icon.VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(
                com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR,
                com.vaadin.flow.component.button.ButtonVariant.LUMO_ICON);
        deleteButton
                .getElement()
                .setProperty(DeckConstants.TITLE_PROPERTY, getTranslation(DeckConstants.FLASHCARD_MENU_DELETE));
        deleteButton.addClickListener(e -> {
            if (deleteFlashcardCallback != null) {
                deleteFlashcardCallback.accept(flashcard);
            }
        });

        buttonsLayout.add(editButton, toggleButton, deleteButton);
        return buttonsLayout;
    }

    /**
     * Refreshes the current page to reload all visible items.
     * Call this after status changes (known/unknown toggle).
     * Preserves current page position.
     *
     * <p>Uses coalescing pattern to prevent multiple redundant refreshes in same server roundtrip.
     * Vaadin's push/polling mechanism can trigger multiple refresh calls during state updates.
     * This pattern ensures only one actual database query executes per roundtrip.
     *
     * <p>Pattern: AtomicBoolean flag + UI.beforeClientResponse() callback
     * Reference: Vaadin Flow common patterns for preventing duplicate operations
     *
     * @see <a href="https://vaadin.com/docs/latest/flow/advanced/server-push">Vaadin Server Push</a>
     */
    public void refreshStatusForCards() {
        if (refreshPending.compareAndSet(false, true)) {
            // Cancel previous timer if exists
            if (refreshTimer != null) {
                refreshTimer.remove();
            }

            // Use beforeClientResponse to coalesce multiple refresh calls
            // All calls within same server roundtrip will be merged into one
            getElement()
                    .getNode()
                    .runWhenAttached(ui -> ui.beforeClientResponse(this, context -> {
                        if (refreshPending.compareAndSet(true, false)) {
                            loadCurrentPage();
                        }
                    }));
        }
    }

    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Cancel pending timer to prevent memory leak
        if (refreshTimer != null) {
            refreshTimer.remove();
            refreshTimer = null;
        }
    }
}
