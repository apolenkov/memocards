package org.apolenkov.application.views.deck.components.deck;

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
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.views.deck.components.DeckConstants;
import org.apolenkov.application.views.deck.components.grid.DeckFlashcardGrid;
import org.apolenkov.application.views.deck.components.grid.DeckGridFilter;
import org.apolenkov.application.views.deck.components.grid.DeckSearchControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for displaying flashcards in a grid with search and filtering capabilities.
 * Coordinates search controls and flashcard grid components.
 */
public final class DeckGrid extends Composite<VerticalLayout> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckGrid.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

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
     */
    public DeckGrid(final StatsService statsServiceParam) {
        this.statsService = statsServiceParam;
        this.searchControls = new DeckSearchControls();
        this.flashcardGrid = new DeckFlashcardGrid(statsService);
        this.addFlashcardButton = new Button();
        this.allFlashcards = new ArrayList<>();
    }

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout grid = new VerticalLayout();

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
        searchControls.setResetCallback(this::handleResetProgress);

        // Flashcard grid callbacks
        flashcardGrid.setToggleKnownCallback(this::handleToggleKnown);
    }

    /**
     * Handles search query changes.
     *
     * @param searchQuery the search query
     */
    private void handleSearch(final String searchQuery) {
        LOGGER.debug("Search query changed: '{}'", searchQuery);
        applyFilter();
    }

    /**
     * Handles filter changes.
     *
     * @param hideKnown whether to hide known cards
     */
    private void handleFilter(final Boolean hideKnown) {
        LOGGER.debug("Filter changed: hideKnown={}", hideKnown);
        applyFilter();
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

            // Audit log for learning progress
            if (!known) {
                AUDIT_LOGGER.info(
                        "Card {} marked as KNOWN in deck {} - User learned: '{}'",
                        flashcard.getId(),
                        currentDeckId,
                        flashcard.getFrontText());
            } else {
                AUDIT_LOGGER.info(
                        "Card {} marked as UNKNOWN in deck {} - User forgot: '{}'",
                        flashcard.getId(),
                        currentDeckId,
                        flashcard.getFrontText());
            }

            statsService.setCardKnown(currentDeckId, flashcard.getId(), !known);
            applyFilter();
        }
    }

    /**
     * Handles the reset progress action.
     */
    private void handleResetProgress() {
        if (currentDeckId != null) {
            LOGGER.info("Resetting progress for deck {}", currentDeckId);

            // Audit log for progress reset
            AUDIT_LOGGER.info(
                    "User reset ALL progress for deck {} - {} cards marked as unknown",
                    currentDeckId,
                    allFlashcards != null ? allFlashcards.size() : 0);

            statsService.resetDeckProgress(currentDeckId);
            applyFilter();
        }
    }

    /**
     * Applies search and filter criteria to the flashcards.
     */
    private void applyFilter() {
        String searchQuery = searchControls.getSearchQuery();
        boolean hideKnown = searchControls.isHideKnown();

        List<Flashcard> filtered =
                DeckGridFilter.applyFilter(allFlashcards, searchQuery, hideKnown, statsService, currentDeckId);

        flashcardGrid.updateData(filtered);
    }

    /**
     * Sets the current deck ID for statistics tracking.
     *
     * @param deckId the deck ID
     */
    public void setCurrentDeckId(final Long deckId) {
        LOGGER.debug("Setting current deck ID: {}", deckId);
        this.currentDeckId = deckId;
        flashcardGrid.setCurrentDeckId(deckId);
    }

    /**
     * Sets the flashcards data for the grid.
     *
     * @param flashcards the list of flashcards to display
     */
    public void setFlashcards(final List<Flashcard> flashcards) {
        LOGGER.debug("Setting {} flashcards for deck {}", flashcards.size(), currentDeckId);
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
     * Creates the layout with add button, search controls, and flashcard grid.
     * Groups buttons logically: Add card (separate row), Search/Filters (separate row).
     *
     * @param container the container to add components to
     */
    private void createLayout(final VerticalLayout container) {
        // Row 1: Add card button (centered)
        HorizontalLayout addCardRow = new HorizontalLayout();
        addCardRow.setWidthFull();
        addCardRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        addCardRow.setAlignItems(FlexComponent.Alignment.CENTER);

        addFlashcardButton.setWidth("auto");
        addCardRow.add(addFlashcardButton);

        // Row 2: Search and filter controls
        HorizontalLayout searchRow = new HorizontalLayout();
        searchRow.setWidthFull();
        searchRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        searchRow.setSpacing(true);
        searchRow.add(searchControls);

        // Add all rows
        container.add(addCardRow, searchRow, flashcardGrid);
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
