package org.apolenkov.application.views.deck.components.decks;

import java.util.List;

import org.apolenkov.application.views.deck.business.DeckCardViewModel;
import org.apolenkov.application.views.deck.components.DeckConstants;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Reusable container component for deck management views.
 * Provides consistent layout, styling, and structure for deck-related content
 * including title, toolbar, and deck list components.
 */
public final class DeckContainer extends VerticalLayout {

    // UI Components
    private final H2 title;
    private final DeckToolbar toolbar;
    private final DeckList deckList;

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
        addClassName("container-md");
        addClassName(DeckConstants.DECKS_SECTION_CLASS);
        addClassName("surface-panel");
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
        // Initialize title content now to avoid using getTranslation in constructor
        title.setText(getTranslation("main.decks"));
        title.addClassName(DeckConstants.DECKS_VIEW_TITLE_CLASS);
        configureLayout();
        addComponents();
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
