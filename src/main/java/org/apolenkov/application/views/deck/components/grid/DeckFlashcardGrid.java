package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component for displaying flashcards in a grid with columns and actions.
 * Provides the main flashcard grid with status indicators and action buttons.
 */
public final class DeckFlashcardGrid extends Grid<Flashcard> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckFlashcardGrid.class);

    // Dependencies
    private final transient StatsService statsService;

    // Callbacks
    private transient Consumer<Flashcard> editFlashcardCallback;
    private transient Consumer<Flashcard> deleteFlashcardCallback;
    private transient Consumer<Flashcard> toggleKnownCallback;

    // Logic
    private transient Long currentDeckId;
    private transient Set<Long> cachedKnownCardIds;

    /**
     * Creates a new DeckFlashcardGrid component.
     *
     * @param statsServiceParam service for statistics tracking
     */
    public DeckFlashcardGrid(final StatsService statsServiceParam) {
        super(Flashcard.class, false);
        this.statsService = statsServiceParam;
    }

    /**
     * Initializes the component when attached to the UI.
     *
     * @param attachEvent the attachment event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        configureGrid();
        addColumns();
    }

    /**
     * Configures the flashcard grid with theme variants.
     */
    private void configureGrid() {
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        setWidthFull();
    }

    /**
     * Adds all necessary columns to the flashcard grid.
     * Uses supplier pattern to load known card IDs only once per grid render.
     */
    private void addColumns() {
        DeckGridColumns.addFrontColumn(this);
        DeckGridColumns.addExampleColumn(this);
        // Supplier pattern: known IDs are loaded once per render, cached, and reused
        DeckGridColumns.addStatusColumn(this, this::getCachedKnownCardIds);
        DeckGridColumns.addActionsColumn(this, editFlashcardCallback, toggleKnownCallback, deleteFlashcardCallback);
    }

    /**
     * Gets cached known card IDs, loading them if not yet cached.
     *
     * @return set of known card IDs
     */
    private Set<Long> getCachedKnownCardIds() {
        if (cachedKnownCardIds == null && currentDeckId != null) {
            cachedKnownCardIds = statsService.getKnownCardIds(currentDeckId);
        }
        return cachedKnownCardIds != null ? cachedKnownCardIds : Set.of();
    }

    /**
     * Sets the current deck ID for statistics tracking.
     *
     * @param deckId the deck ID
     */
    public void setCurrentDeckId(final Long deckId) {
        LOGGER.debug("Setting current deck ID: {}", deckId);
        this.currentDeckId = deckId;
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
     * Updates the grid data provider with filtered flashcards.
     * Invalidates cache to force reload of known card IDs on next render.
     *
     * @param filteredFlashcards the filtered flashcards to display
     */
    public void updateData(final List<Flashcard> filteredFlashcards) {
        // Invalidate cache to ensure fresh data on next render
        cachedKnownCardIds = null;

        // Data
        ListDataProvider<Flashcard> flashcardsDataProvider =
                new ListDataProvider<>(new ArrayList<>(filteredFlashcards));
        setDataProvider(flashcardsDataProvider);
    }
}
