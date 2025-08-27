package org.apolenkov.application.views.home;

import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.query.DeckQueryService;
import org.springframework.stereotype.Component;

/**
 * Presenter for the home view, handling deck listing operations.
 */
@Component
public class HomePresenter {

    private final DeckQueryService deckQueryService;

    /**
     * Creates a new HomePresenter with the specified deck query service.
     *
     * @param deckQueryServiceValue the service for querying deck data (non-null)
     * @throws IllegalArgumentException if deckQueryService is null
     */
    public HomePresenter(final DeckQueryService deckQueryServiceValue) {
        if (deckQueryServiceValue == null) {
            throw new IllegalArgumentException("DeckQueryService cannot be null");
        }
        this.deckQueryService = deckQueryServiceValue;
    }

    /**
     * Lists decks for the current user based on an optional search query.
     *
     * @param query the search query to filter decks, maybe null or empty
     * @return a list of deck view models for the current user, never null (maybe empty)
     */
    public List<DeckCardViewModel> listDecksForCurrentUser(final String query) {
        List<Deck> decks = deckQueryService.listDecksForCurrentUser(query);
        return decks.stream().map(deckQueryService::toViewModel).toList();
    }
}
