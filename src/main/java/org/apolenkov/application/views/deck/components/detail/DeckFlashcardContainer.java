package org.apolenkov.application.views.deck.components.detail;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.StatsService;
import org.apolenkov.application.views.deck.components.grid.DeckFlashcardList;
import org.apolenkov.application.views.deck.components.grid.DeckSearchControls;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container component for displaying flashcards with search, filtering, and pagination.
 * Coordinates search controls and flashcard list with explicit pagination controls.
 * Uses lazy loading for efficient handling of large collections (500+ cards).
 */
public final class DeckFlashcardContainer extends Composite<VerticalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckFlashcardContainer.class);

    // Dependencies
    private final transient StatsService statsService;

    // UI Components
    private final DeckSearchControls searchControls;
    private final DeckFlashcardList flashcardList;

    // Data
    private transient Long currentDeckId;
    private transient FilterOption currentFilterOption;

    /**
     * Creates a new DeckFlashcardContainer component.
     *
     * @param statsServiceParam service for statistics tracking
     * @param flashcardUseCaseParam use case for flashcard operations
     * @param searchDebounceMs debouncing timeout for search field
     * @param pageSize number of items per page in flashcard list
     */
    public DeckFlashcardContainer(
            final StatsService statsServiceParam,
            final org.apolenkov.application.domain.usecase.FlashcardUseCase flashcardUseCaseParam,
            final int searchDebounceMs,
            final int pageSize) {
        this.statsService = statsServiceParam;
        this.searchControls = new DeckSearchControls(searchDebounceMs);
        this.flashcardList = new DeckFlashcardList(statsService, flashcardUseCaseParam, pageSize);
        this.currentFilterOption = FilterOption.UNKNOWN_ONLY; // Default: hide known
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout grid = new VerticalLayout();
        grid.setPadding(false);
        grid.setSpacing(true);
        grid.setWidthFull();
        grid.setAlignItems(FlexComponent.Alignment.CENTER);
        grid.addClassName(DeckConstants.DECK_CENTERED_SECTION_CLASS);
        grid.addClassName(DeckConstants.DECK_GRID_SECTION_CLASS);

        setupCallbacks();
        createLayout(grid);
        applyFilter();

        return grid;
    }

    /**
     * Sets up all callbacks for search controls and flashcard grid.
     */
    private void setupCallbacks() {
        // Search controls callbacks
        searchControls.setSearchCallback(this::handleSearch);
        searchControls.addFilterChangeListener(this::handleFilterChange);

        // Flashcard list callbacks
        flashcardList.setToggleKnownCallback(this::handleToggleKnown);
    }

    /**
     * Handles filter option changes.
     *
     * @param filterOption the new filter option
     */
    private void handleFilterChange(final FilterOption filterOption) {
        this.currentFilterOption = filterOption != null ? filterOption : FilterOption.ALL;
        applyFilter();
    }

    /**
     * Handles search query changes.
     *
     * @param searchQuery the search query
     */
    private void handleSearch(final String searchQuery) {
        // Apply filter with the provided search query
        applyFilterWithParams(searchQuery, currentFilterOption);
    }

    /**
     * Handles the toggle known action for a flashcard.
     *
     * @param flashcard the flashcard to toggle
     */
    private void handleToggleKnown(final Flashcard flashcard) {
        if (currentDeckId != null) {
            boolean known = statsService.isCardKnown(currentDeckId, flashcard.getId());
            LOGGER.debug("Toggling known status for card {}: {} -> {}", flashcard.getId(), known, !known);

            statsService.setCardKnown(currentDeckId, flashcard.getId(), !known);

            // Invalidate list cache to force fresh data loading
            flashcardList.invalidateCache();

            // Refresh status for the specific card that changed
            flashcardList.refreshStatusForCards();

            applyFilter();
        }
    }

    /**
     * Resets progress for the current deck by clearing all known/unknown statuses.
     * This method should be called from the parent component after user confirmation.
     *
     * <p>The method performs the following actions:
     * <ul>
     *   <li>Resets all card statuses in the statistics service</li>
     *   <li>Invalidates the grid cache to force fresh data loading</li>
     *   <li>Refreshes status indicators for all cards in the grid</li>
     *   <li>Reapplies current search and filter criteria</li>
     * </ul>
     *
     * <p>Note: This operation has no effect if currentDeckId is null.
     */
    public void resetProgress() {
        if (currentDeckId != null) {
            LOGGER.debug("Resetting progress for deck {}", currentDeckId);

            statsService.resetDeckProgress(currentDeckId);

            // Invalidate list cache to force fresh data loading
            flashcardList.invalidateCache();

            // Refresh all cards since all statuses changed
            flashcardList.refreshStatusForCards();

            applyFilter();
        }
    }

    /**
     * Applies search and filter criteria to the flashcards.
     * Passes known card IDs to list to avoid duplicate queries.
     */
    private void applyFilter() {
        applyFilterWithParams(searchControls.getSearchQuery(), currentFilterOption);
    }

    /**
     * Applies search and filter criteria to the flashcards with specific parameters.
     * Uses lazy loading through data provider instead of in-memory filtering.
     *
     * @param searchQuery the search query to apply
     * @param filterOption the filter option to apply
     */
    private void applyFilterWithParams(final String searchQuery, final FilterOption filterOption) {
        // Update filter in data provider (triggers lazy loading from backend)
        flashcardList.updateFilter(searchQuery, filterOption);
    }

    /**
     * Sets the current deck ID for statistics tracking.
     *
     * @param deckId the deck ID
     */
    public void setCurrentDeckId(final Long deckId) {
        this.currentDeckId = deckId;
        flashcardList.setCurrentDeckId(deckId);
    }

    /**
     * Refreshes the data in the grid.
     * Call after flashcard create/update/delete operations.
     */
    public void refreshData() {
        flashcardList.refreshStatusForCards();
    }

    /**
     * Sets the edit flashcard callback.
     *
     * @param callback the callback to execute when editing a flashcard
     */
    public void setEditFlashcardCallback(final Consumer<Flashcard> callback) {
        if (flashcardList != null) {
            flashcardList.setEditFlashcardCallback(callback);
        }
    }

    /**
     * Sets the delete flashcard callback.
     *
     * @param callback the callback to execute when deleting a flashcard
     */
    public void setDeleteFlashcardCallback(final Consumer<Flashcard> callback) {
        if (flashcardList != null) {
            flashcardList.setDeleteFlashcardCallback(callback);
        }
    }

    /**
     * Adds a listener for filter value changes.
     *
     * @param callback the callback to execute when filter value changes
     * @return registration for removing the listener
     */
    public Registration addFilterChangeListener(final java.util.function.Consumer<FilterOption> callback) {
        return searchControls.addFilterChangeListener(callback);
    }

    /**
     * Creates the layout with search controls and flashcard list.
     * Only search controls at the top, list below.
     *
     * @param container the container to add components to
     */
    private void createLayout(final VerticalLayout container) {
        container.setWidthFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        // Search controls row
        HorizontalLayout controlsRow = new HorizontalLayout();
        controlsRow.setWidthFull();
        controlsRow.setAlignItems(FlexComponent.Alignment.CENTER);
        controlsRow.setSpacing(true);

        // Only search controls
        controlsRow.add(searchControls);

        // Add row and list
        container.add(controlsRow, flashcardList);
    }
}
