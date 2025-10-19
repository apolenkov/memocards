package org.apolenkov.application.views.deck.components.grid;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.StatsService;
import org.apolenkov.application.views.deck.constants.DeckConstants;

/**
 * Component for displaying flashcards as a list of cards instead of a grid.
 * Each flashcard is displayed as a card with front text, example, status, and actions.
 */
public final class DeckFlashcardList extends VerticalLayout {

    // Dependencies
    private final transient StatsService statsService;

    // Callbacks (stored for menu creation)
    private transient Consumer<Flashcard> editFlashcardCallback;
    private transient Consumer<Flashcard> deleteFlashcardCallback;
    private transient Consumer<Flashcard> toggleKnownCallback;

    // Logic
    private transient Long currentDeckId;
    private transient Set<Long> cachedKnownCardIds;
    private transient List<Flashcard> currentFlashcards;

    // Lifecycle
    private boolean hasBeenInitialized = false;

    /**
     * Creates a new DeckFlashcardList component.
     *
     * @param statsServiceParam service for statistics tracking
     */
    public DeckFlashcardList(final StatsService statsServiceParam) {
        this.statsService = statsServiceParam;
        this.currentFlashcards = new ArrayList<>();

        setWidthFull();
        setPadding(false);
        setSpacing(true);
        addClassName("deck-flashcard-list");
    }

    /**
     * Initializes the component when attached to the UI.
     * Uses hasBeenInitialized flag to prevent duplicate initialization on reattach.
     *
     * @param attachEvent the attachment event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (!hasBeenInitialized) {
            hasBeenInitialized = true;
            configureList();
        }
    }

    /**
     * Configures the flashcard list styling.
     */
    private void configureList() {
        addClassName("deck-flashcard-list");
    }

    /**
     * Sets the current deck ID for statistics tracking.
     *
     * @param deckId the deck ID
     */
    public void setCurrentDeckId(final Long deckId) {
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
     * Updates the list data with filtered flashcards.
     * Caches known card IDs to reuse in status indicators (avoids duplicate DB query).
     *
     * @param filteredFlashcards the filtered flashcards to display
     * @param knownCardIds the set of known card IDs (pre-loaded by filter)
     */
    public void updateData(final List<Flashcard> filteredFlashcards, final Set<Long> knownCardIds) {
        // Cache known card IDs from filter to reuse in status indicators
        this.cachedKnownCardIds = knownCardIds;
        this.currentFlashcards = new ArrayList<>(filteredFlashcards);

        // Clear existing cards
        removeAll();

        // Create card components for each flashcard
        for (Flashcard flashcard : filteredFlashcards) {
            add(createFlashcardCard(flashcard));
        }
    }

    /**
     * Creates a card component for a single flashcard.
     *
     * @param flashcard the flashcard to create a card for
     * @return the card component
     */
    private Div createFlashcardCard(final Flashcard flashcard) {
        Div card = new Div();
        card.addClassName("flashcard-card");

        // Add known/unknown class for styling (green/blue left border)
        boolean isKnown = getCachedKnownCardIds().contains(flashcard.getId());
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
     * Refreshes the status indicators to show updated known status.
     * Forces re-render of cards by refreshing the entire list.
     */
    public void refreshStatusForCards() {
        // For simplicity, refresh all cards when status changes
        // In a more complex implementation, we could track individual cards
        if (!currentFlashcards.isEmpty()) {
            updateData(currentFlashcards, getCachedKnownCardIds());
        }
    }

    /**
     * Invalidates the local cache to force fresh data loading.
     * Call this after known status changes to ensure UI shows updated status.
     */
    public void invalidateCache() {
        this.cachedKnownCardIds = null;
    }
}
