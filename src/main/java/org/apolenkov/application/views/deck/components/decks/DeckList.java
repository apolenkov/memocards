package org.apolenkov.application.views.deck.components.decks;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.apolenkov.application.views.deck.business.DeckCardViewModel;
import org.apolenkov.application.views.deck.components.DeckConstants;

/**
 * Reusable component for displaying a list of deck cards.
 * Handles deck display, empty state management, and provides
 * clean separation between data and presentation logic.
 */
public final class DeckList extends Composite<VerticalLayout> {

    @Override
    protected VerticalLayout initContent() {
        VerticalLayout deckList = new VerticalLayout();
        deckList.setPadding(false);
        deckList.setSpacing(true);
        deckList.setWidthFull();
        deckList.setAlignItems(FlexComponent.Alignment.CENTER);
        deckList.addClassName(DeckConstants.DECK_LIST_CLASS);
        return deckList;
    }

    /**
     * Refreshes the deck list with new data.
     * Clears existing content and displays either deck cards or empty state message.
     *
     * @param decks the list of deck view models to display
     */
    public void refreshDecks(final List<DeckCardViewModel> decks) {
        getContent().removeAll();

        if (decks == null || decks.isEmpty()) {
            showEmptyState();
            return;
        }

        addDeckCards(decks);
    }

    /**
     * Displays the empty state message when no decks are found.
     * Shows a localized message indicating no search results.
     */
    private void showEmptyState() {
        Span emptyMessage = new Span(getTranslation(DeckConstants.HOME_SEARCH_NO_RESULTS));
        emptyMessage.addClassName(DeckConstants.DECKS_EMPTY_MESSAGE_CLASS);
        getContent().add(emptyMessage);
    }

    /**
     * Adds deck cards to the list.
     * Creates DeckCard components for each deck view model.
     *
     * @param decks the list of deck view models to display
     */
    private void addDeckCards(final List<DeckCardViewModel> decks) {
        decks.stream().map(DeckCard::new).forEach(card -> getContent().add(card));
    }
}
