package org.apolenkov.application.views.deck.components.detail;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.service.stats.StatsService;
import org.apolenkov.application.views.deck.components.grid.DeckCardList;
import org.apolenkov.application.views.deck.components.grid.DeckSearchControls;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container component for displaying cards with search, filtering, and pagination.
 * Coordinates search controls and card list with explicit pagination controls.
 * Uses lazy loading for efficient handling of large collections (500+ cards).
 */
public final class DeckCardContainer extends Composite<VerticalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckCardContainer.class);

    // Dependencies
    private final transient StatsService statsService;

    // UI Components
    private final DeckSearchControls searchControls;
    private final DeckCardList cardList;

    // Data
    private transient Long currentDeckId;
    private transient FilterOption currentFilterOption;

    /**
     * Creates a new DeckCardContainer component.
     *
     * @param statsServiceParam service for statistics tracking
     * @param cardUseCaseParam use case for card operations
     * @param searchDebounceMs debouncing timeout for search field
     * @param pageSize number of items per page in card list
     */
    public DeckCardContainer(
            final StatsService statsServiceParam,
            final org.apolenkov.application.domain.usecase.CardUseCase cardUseCaseParam,
            final int searchDebounceMs,
            final int pageSize) {
        this.statsService = statsServiceParam;
        this.searchControls = new DeckSearchControls(searchDebounceMs);
        this.cardList = new DeckCardList(statsService, cardUseCaseParam, pageSize);
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
     * Sets up all callbacks for search controls and card grid.
     */
    private void setupCallbacks() {
        // Search controls callbacks
        searchControls.setSearchCallback(this::handleSearch);
        searchControls.addFilterChangeListener(this::handleFilterChange);

        // Card list callbacks
        cardList.setToggleKnownCallback(this::handleToggleKnown);
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
     * Handles the toggle known action for a card.
     * Uses toggleCardKnown() to avoid redundant isCardKnown() check.
     *
     * @param card the card to toggle
     */
    private void handleToggleKnown(final Card card) {
        if (currentDeckId != null) {
            // This saves one SQL query by checking and updating in single transaction
            statsService.toggleCardKnown(currentDeckId, card.getId());

            // Single refresh - cache auto-invalidated via ProgressChangedEvent
            // refreshStatusForCards() uses debouncing to prevent multiple redundant refreshes
            cardList.refreshStatusForCards();
        }
    }

    /**
     * Resets progress for the current deck by clearing all known/unknown statuses.
     * This method should be called from the parent component after user confirmation.
     *
     * <p>The method performs the following actions:
     * <ul>
     *   <li>Resets all card statuses in the statistics service</li>
     *   <li>Refreshes status indicators for all cards in the grid</li>
     *   <li>Current search and filter criteria are automatically reapplied</li>
     * </ul>
     *
     * <p>Note: This operation has no effect if currentDeckId is null.
     * Cache is auto-invalidated via ProgressChangedEvent (event-driven approach).
     */
    public void resetProgress() {
        if (currentDeckId != null) {
            LOGGER.debug("Resetting progress for deck {}", currentDeckId);

            statsService.resetDeckProgress(currentDeckId);

            // Single refresh - cache auto-invalidated via ProgressChangedEvent
            cardList.refreshStatusForCards();
        }
    }

    /**
     * Applies search and filter criteria to the cards.
     * Passes known card IDs to list to avoid duplicate queries.
     */
    private void applyFilter() {
        applyFilterWithParams(searchControls.getSearchQuery(), currentFilterOption);
    }

    /**
     * Applies search and filter criteria to the cards with specific parameters.
     * Uses lazy loading through data provider instead of in-memory filtering.
     *
     * @param searchQuery the search query to apply
     * @param filterOption the filter option to apply
     */
    private void applyFilterWithParams(final String searchQuery, final FilterOption filterOption) {
        // Update filter in data provider (triggers lazy loading from backend)
        cardList.updateFilter(searchQuery, filterOption);
    }

    /**
     * Sets the current deck ID for statistics tracking.
     *
     * @param deckId the deck ID
     */
    public void setCurrentDeckId(final Long deckId) {
        this.currentDeckId = deckId;
        cardList.setCurrentDeckId(deckId);
    }

    /**
     * Refreshes the data in the grid.
     * Call after card create/update/delete operations.
     */
    public void refreshData() {
        cardList.refreshStatusForCards();
    }

    /**
     * Refreshes the data and resets to first page.
     * Call after adding or removing cards to ensure proper pagination.
     */
    public void refreshDataAndResetPage() {
        cardList.refreshDataAndResetPage();
    }

    /**
     * Sets the edit card callback.
     *
     * @param callback the callback to execute when editing a card
     */
    public void setEditCardCallback(final Consumer<Card> callback) {
        if (cardList != null) {
            cardList.setEditCardCallback(callback);
        }
    }

    /**
     * Sets the delete card callback.
     *
     * @param callback the callback to execute when deleting a card
     */
    public void setDeleteCardCallback(final Consumer<Card> callback) {
        if (cardList != null) {
            cardList.setDeleteCardCallback(callback);
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
     * Creates the layout with search controls and card list.
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
        container.add(controlsRow, cardList);
    }
}
