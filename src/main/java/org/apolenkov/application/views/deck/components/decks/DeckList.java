package org.apolenkov.application.views.deck.components.decks;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.apolenkov.application.views.deck.business.DeckCardViewModel;
import org.apolenkov.application.views.deck.components.DeckConstants;

/**
 * Reusable component for displaying a list of deck cards.
 * Handles deck display, empty state management, and provides
 * clean separation between data and presentation logic.
 */
public final class DeckList extends VerticalLayout {

    /**
     * Creates a new DeckList with default configuration.
     * Initialization is deferred to onAttach to avoid this-escape warnings.
     */
    public DeckList() {
        // Intentionally left blank
    }

    /**
     * Configures the deck list layout with proper styling.
     * Sets up consistent spacing, alignment, and CSS classes.
     */
    private void configureLayout() {
        setPadding(false);
        setSpacing(true);
        setWidthFull();
        setAlignItems(Alignment.CENTER);
        addClassName(DeckConstants.DECK_LIST_CLASS);
    }

    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        configureLayout();
    }

    /**
     * Refreshes the deck list with new data.
     * Clears existing content and displays either deck cards or empty state message.
     *
     * @param decks the list of deck view models to display
     */
    public void refreshDecks(final List<DeckCardViewModel> decks) {
        removeAll();

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
        add(emptyMessage);
    }

    /**
     * Adds deck cards to the list.
     * Creates DeckCard components for each deck view model.
     *
     * @param decks the list of deck view models to display
     */
    private void addDeckCards(final List<DeckCardViewModel> decks) {
        decks.stream().map(DeckCard::new).forEach(this::add);
    }
}
