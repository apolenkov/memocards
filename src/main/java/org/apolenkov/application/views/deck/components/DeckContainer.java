package org.apolenkov.application.views.deck.components;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.apolenkov.application.views.deck.business.DeckCardViewModel;

/**
 * Reusable container component for deck management views.
 * Provides consistent layout, styling, and structure for deck-related content
 * including title, toolbar, and deck list components.
 */
public final class DeckContainer extends VerticalLayout {

    // Constants
    private static final String DECKS_TITLE_KEY = "main.decks";
    private static final String CONTAINER_MD_CLASS = "container-md";
    private static final String DECKS_SECTION_CLASS = "decks-section";
    private static final String SURFACE_PANEL_CLASS = "surface-panel";
    private static final String DECKS_TITLE_CLASS = "decks-view__title";

    // UI Components
    private final H2 title;
    private final DeckToolbar toolbar;
    private final DeckList deckList;

    // State
    private boolean initialized;

    /**
     * Creates a new DeckContainer with all required components.
     * Initializes title, toolbar, and deck list with proper configuration.
     */
    public DeckContainer() {
        this.title = new H2();
        this.toolbar = new DeckToolbar();
        this.deckList = new DeckList();
    }

    // Title content is set in onAttach to avoid using getTranslation in constructor

    /**
     * Configures the container layout with proper styling.
     * Applies consistent spacing, alignment, and CSS classes.
     */
    private void configureLayout() {
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setWidthFull();
        addClassName(CONTAINER_MD_CLASS);
        addClassName(DECKS_SECTION_CLASS);
        addClassName(SURFACE_PANEL_CLASS);
    }

    /**
     * Adds all components to the container layout.
     * Arranges title, toolbar, and deck list in proper order.
     */
    private void addComponents() {
        add(title, toolbar, deckList);
    }

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (initialized) {
            return;
        }
        // Initialize title content now to avoid using getTranslation in constructor
        title.setText(getTranslation(DECKS_TITLE_KEY));
        title.addClassName(DECKS_TITLE_CLASS);
        configureLayout();
        addComponents();
        initialized = true;
    }

    /**
     * Gets the toolbar component for external configuration.
     * Allows parent components to set up event listeners.
     *
     * @return the DeckToolbar component
     */
    public DeckToolbar getToolbar() {
        return toolbar;
    }

    /**
     * Refreshes the deck list with new data.
     * Delegates to the deck list component for data refresh.
     *
     * @param decks the list of deck view models to display
     */
    public void refreshDecks(final List<DeckCardViewModel> decks) {
        deckList.refreshDecks(decks);
    }
}
