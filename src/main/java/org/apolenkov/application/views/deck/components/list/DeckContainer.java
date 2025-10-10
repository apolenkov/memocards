package org.apolenkov.application.views.deck.components.list;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.apolenkov.application.views.deck.business.DeckCardViewModel;
import org.apolenkov.application.views.deck.constants.DeckConstants;

/**
 * Reusable container component for deck management views.
 * Provides consistent layout, styling, and structure for deck-related content
 * including title, toolbar, and deck list components.
 */
public final class DeckContainer extends Composite<VerticalLayout> {

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

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setWidthFull();
        container.addClassName(DeckConstants.CONTAINER_MD_CLASS);
        container.addClassName(DeckConstants.DECKS_SECTION_CLASS);
        container.addClassName(DeckConstants.SURFACE_PANEL_CLASS);

        // Initialize title content
        title.setText(getTranslation(DeckConstants.DECKS_TITLE_KEY));
        title.addClassName(DeckConstants.DECKS_VIEW_TITLE_CLASS);

        container.add(title, toolbar, deckList);
        return container;
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
