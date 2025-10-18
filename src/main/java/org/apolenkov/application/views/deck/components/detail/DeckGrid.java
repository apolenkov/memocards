package org.apolenkov.application.views.deck.components.detail;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.StatsService;
import org.apolenkov.application.views.deck.components.grid.DeckFlashcardGrid;
import org.apolenkov.application.views.deck.components.grid.DeckGridFilter;
import org.apolenkov.application.views.deck.components.grid.DeckSearchControls;
import org.apolenkov.application.views.deck.constants.DeckConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for displaying flashcards in a grid with search and filtering capabilities.
 * Coordinates search controls and flashcard grid components.
 */
public final class DeckGrid extends Composite<VerticalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckGrid.class);

    // Dependencies
    private final transient StatsService statsService;

    // UI Components
    private final DeckSearchControls searchControls;
    private final DeckFlashcardGrid flashcardGrid;
    private final Button addFlashcardButton;

    // Data
    private transient Long currentDeckId;
    private transient List<Flashcard> allFlashcards;

    // Event Registrations
    private Registration addFlashcardClickListenerRegistration;

    /**
     * Creates a new DeckGrid component.
     *
     * @param statsServiceParam service for statistics tracking
     * @param searchDebounceMs debouncing timeout for search field
     */
    public DeckGrid(final StatsService statsServiceParam, final int searchDebounceMs) {
        this.statsService = statsServiceParam;
        this.searchControls = new DeckSearchControls(searchDebounceMs);
        this.flashcardGrid = new DeckFlashcardGrid(statsService);
        this.addFlashcardButton = new Button();
        this.allFlashcards = new ArrayList<>();
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

        createAddFlashcardButton();
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
        searchControls.setFilterCallback(this::handleFilter);

        // Flashcard grid callbacks
        flashcardGrid.setToggleKnownCallback(this::handleToggleKnown);
    }

    /**
     * Handles search query changes.
     *
     * @param searchQuery the search query
     */
    private void handleSearch(final String searchQuery) {
        // Apply filter with the provided search query
        applyFilterWithParams(searchQuery, searchControls.isHideKnown());
    }

    /**
     * Handles filter changes.
     *
     * @param hideKnown whether to hide known cards
     */
    private void handleFilter(final Boolean hideKnown) {
        // Apply filter with the provided hide known setting
        applyFilterWithParams(searchControls.getSearchQuery(), hideKnown);
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

            // Invalidate grid cache to force fresh data loading
            flashcardGrid.invalidateCache();

            // Refresh status for the specific card that changed
            flashcardGrid.refreshStatusForCards(Set.of(flashcard.getId()));

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

            // Invalidate grid cache to force fresh data loading
            flashcardGrid.invalidateCache();

            // Refresh all cards since all statuses changed
            if (allFlashcards != null) {
                Set<Long> allCardIds =
                        allFlashcards.stream().map(Flashcard::getId).collect(java.util.stream.Collectors.toSet());
                flashcardGrid.refreshStatusForCards(allCardIds);
            }

            applyFilter();
        }
    }

    /**
     * Applies search and filter criteria to the flashcards.
     * Passes known card IDs to grid to avoid duplicate queries.
     */
    private void applyFilter() {
        applyFilterWithParams(searchControls.getSearchQuery(), searchControls.isHideKnown());
    }

    /**
     * Applies search and filter criteria to the flashcards with specific parameters.
     * Passes known card IDs to grid to avoid duplicate queries.
     *
     * @param searchQuery the search query to apply
     * @param hideKnown whether to hide known cards
     */
    private void applyFilterWithParams(final String searchQuery, final boolean hideKnown) {
        var filterResult =
                DeckGridFilter.applyFilter(allFlashcards, searchQuery, hideKnown, statsService, currentDeckId);

        // Pass known card IDs to grid to reuse in status column (avoids duplicate DB query)
        flashcardGrid.updateData(filterResult.filteredFlashcards(), filterResult.knownCardIds());
    }

    /**
     * Sets the current deck ID for statistics tracking.
     *
     * @param deckId the deck ID
     */
    public void setCurrentDeckId(final Long deckId) {
        this.currentDeckId = deckId;
        flashcardGrid.setCurrentDeckId(deckId);
    }

    /**
     * Sets the flashcards data for the grid.
     *
     * @param flashcards the list of flashcards to display
     */
    public void setFlashcards(final List<Flashcard> flashcards) {
        this.allFlashcards = new ArrayList<>(flashcards);
        applyFilter();
    }

    /**
     * Updates a flashcard in the local data and refreshes the grid.
     *
     * @param updatedFlashcard the updated flashcard
     */
    public void updateFlashcard(final Flashcard updatedFlashcard) {
        if (allFlashcards != null) {
            // Find and replace the flashcard in the local list
            for (int i = 0; i < allFlashcards.size(); i++) {
                if (allFlashcards.get(i).getId().equals(updatedFlashcard.getId())) {
                    allFlashcards.set(i, updatedFlashcard);
                    break;
                }
            }
            applyFilter();
        }
    }

    /**
     * Removes a flashcard from the local data and refreshes the grid.
     *
     * @param flashcardId the ID of the flashcard to remove
     */
    public void removeFlashcard(final Long flashcardId) {
        if (allFlashcards != null) {
            allFlashcards.removeIf(flashcard -> flashcard.getId().equals(flashcardId));
            applyFilter();
        }
    }

    /**
     * Sets the edit flashcard callback.
     *
     * @param callback the callback to execute when editing a flashcard
     */
    public void setEditFlashcardCallback(final Consumer<Flashcard> callback) {
        if (flashcardGrid != null) {
            flashcardGrid.setEditFlashcardCallback(callback);
        }
    }

    /**
     * Sets the delete flashcard callback.
     *
     * @param callback the callback to execute when deleting a flashcard
     */
    public void setDeleteFlashcardCallback(final Consumer<Flashcard> callback) {
        if (flashcardGrid != null) {
            flashcardGrid.setDeleteFlashcardCallback(callback);
        }
    }

    /**
     * Creates the add flashcard button.
     */
    private void createAddFlashcardButton() {
        addFlashcardButton.setText(getTranslation(DeckConstants.DECK_ADD_CARD));
        addFlashcardButton.setIcon(VaadinIcon.PLUS.create());
        addFlashcardButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    }

    /**
     * Creates the layout with search controls, add button, and flashcard grid.
     * Groups all controls in one row: Search/Filters (left), Add card button (right).
     *
     * @param container the container to add components to
     */
    private void createLayout(final VerticalLayout container) {
        container.setWidthFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);

        // Single row: Search, filter, and add button aligned to edges
        HorizontalLayout controlsRow = new HorizontalLayout();
        controlsRow.setWidthFull();
        controlsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        controlsRow.setAlignItems(FlexComponent.Alignment.CENTER);
        controlsRow.setSpacing(true);

        addFlashcardButton.setWidth("auto");

        // Left side: Search controls (search field + checkbox)
        // Right side: Add button
        controlsRow.add(searchControls, addFlashcardButton);

        // Add row and grid
        container.add(controlsRow, flashcardGrid);
    }

    /**
     * Sets the add flashcard click listener.
     *
     * @param listener the click listener
     * @return registration for removing the listener
     */
    public Registration addAddFlashcardClickListener(final ComponentEventListener<ClickEvent<Button>> listener) {
        if (addFlashcardClickListenerRegistration != null) {
            addFlashcardClickListenerRegistration.remove();
        }
        addFlashcardClickListenerRegistration = addFlashcardButton.addClickListener(listener);
        return addFlashcardClickListenerRegistration;
    }

    /**
     * Cleans up event listeners when the component is detached.
     * Prevents memory leaks by removing event listener registrations.
     *
     * @param detachEvent the detach event
     */
    @Override
    protected void onDetach(final DetachEvent detachEvent) {
        if (addFlashcardClickListenerRegistration != null) {
            addFlashcardClickListenerRegistration.remove();
            addFlashcardClickListenerRegistration = null;
        }
        super.onDetach(detachEvent);
    }
}
