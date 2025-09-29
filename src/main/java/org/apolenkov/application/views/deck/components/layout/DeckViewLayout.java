package org.apolenkov.application.views.deck.components.layout;

import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.views.deck.components.deck.DeckActions;
import org.apolenkov.application.views.deck.components.deck.DeckGrid;
import org.apolenkov.application.views.deck.components.deck.DeckHeader;
import org.apolenkov.application.views.deck.components.deck.DeckInfo;

/**
 * Layout component for DeckView page structure.
 * Handles creation and arrangement of deck page components.
 *
 * <p>Features:
 * <ul>
 *   <li>Creates main deck content layout</li>
 *   <li>Manages component creation and arrangement</li>
 *   <li>Provides access to created components</li>
 * </ul>
 */
public final class DeckViewLayout {

    // Styles
    private static final String SURFACE_PANEL_CLASS = "surface-panel";
    private static final String CONTAINER_MD_CLASS = "container-md";
    private static final String DECK_VIEW_SECTION_CLASS = "deck-view__section";

    // Dependencies
    private final StatsService statsService;

    // Components
    private DeckHeader deckHeader;
    private DeckInfo deckInfo;
    private DeckActions deckActions;
    private DeckGrid deckGrid;

    // Callbacks
    private Consumer<Flashcard> onEditFlashcardCallback;
    private Consumer<Flashcard> onDeleteFlashcardCallback;

    /**
     * Creates a new DeckViewLayout with required dependencies.
     *
     * @param statsServiceParam service for statistics tracking
     */
    public DeckViewLayout(final StatsService statsServiceParam) {
        this.statsService = statsServiceParam;
    }

    /**
     * Sets the edit flashcard callback.
     *
     * @param callback the callback for editing flashcards
     */
    public void setEditFlashcardCallback(final Consumer<Flashcard> callback) {
        this.onEditFlashcardCallback = callback;
    }

    /**
     * Sets the delete flashcard callback.
     *
     * @param callback the callback for deleting flashcards
     */
    public void setDeleteFlashcardCallback(final Consumer<Flashcard> callback) {
        this.onDeleteFlashcardCallback = callback;
    }

    /**
     * Creates the main deck content layout.
     *
     * @return the main content container
     */
    public VerticalLayout createDeckContent() {
        // Main content container with consistent width and centering
        VerticalLayout contentContainer = new VerticalLayout();
        contentContainer.setSpacing(true);
        contentContainer.setWidthFull();
        contentContainer.addClassName(CONTAINER_MD_CLASS);
        contentContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        // Primary content section with surface styling
        VerticalLayout pageSection = new VerticalLayout();
        pageSection.setSpacing(true);
        pageSection.setPadding(true);
        pageSection.setWidthFull();
        pageSection.addClassName(DECK_VIEW_SECTION_CLASS);
        pageSection.addClassName(SURFACE_PANEL_CLASS);
        pageSection.addClassName(CONTAINER_MD_CLASS);

        createHeader(pageSection);
        createDeckInfo(pageSection);
        createActions(pageSection);
        createFlashcardsGrid(pageSection);

        contentContainer.add(pageSection);
        return contentContainer;
    }

    /**
     * Creates the header section with navigation and deck title.
     *
     * @param container the container to add the header to
     */
    private void createHeader(final VerticalLayout container) {
        deckHeader = new DeckHeader();
        container.add(deckHeader);
    }

    /**
     * Creates the deck information section with description.
     *
     * @param container the container to add the deck info to
     */
    private void createDeckInfo(final VerticalLayout container) {
        deckInfo = new DeckInfo();
        container.add(deckInfo);
    }

    /**
     * Creates the actions section with practice, add, edit and delete buttons.
     *
     * @param container the container to add the actions to
     */
    private void createActions(final VerticalLayout container) {
        deckActions = new DeckActions();
        container.add(deckActions);
    }

    /**
     * Creates the flashcards grid section with search and filtering.
     *
     * @param container the container to add the grid to
     */
    private void createFlashcardsGrid(final VerticalLayout container) {
        deckGrid = new DeckGrid(statsService);
        // Set callbacks immediately after creation
        if (onEditFlashcardCallback != null) {
            deckGrid.setEditFlashcardCallback(onEditFlashcardCallback);
        }
        if (onDeleteFlashcardCallback != null) {
            deckGrid.setDeleteFlashcardCallback(onDeleteFlashcardCallback);
        }
        container.add(deckGrid);
    }

    /**
     * Gets the created deck header component.
     *
     * @return the deck header component
     */
    public DeckHeader getDeckHeader() {
        return deckHeader;
    }

    /**
     * Gets the created deck info component.
     *
     * @return the deck info component
     */
    public DeckInfo getDeckInfo() {
        return deckInfo;
    }

    /**
     * Gets the created deck actions component.
     *
     * @return the deck actions component
     */
    public DeckActions getDeckActions() {
        return deckActions;
    }

    /**
     * Gets the created deck grid component.
     *
     * @return the deck grid component
     */
    public DeckGrid getDeckGrid() {
        return deckGrid;
    }
}
