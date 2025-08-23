package org.apolenkov.application.views.home;

import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.query.DeckQueryService;
import org.springframework.stereotype.Component;

/**
 * Presenter for the home view, handling deck listing operations.
 * This presenter serves as an intermediary between the home view
 * and the deck query service, converting domain objects to view models
 * suitable for presentation in the UI.
 */
@Component
public class HomePresenter {

    private final DeckQueryService deckQueryService;

    /**
     * Creates a new HomePresenter with the specified deck query service.
     *
     * @param deckQueryService the service for querying deck data
     */
    public HomePresenter(DeckQueryService deckQueryService) {
        this.deckQueryService = deckQueryService;
    }

    /**
     * Lists decks for the current user based on an optional search query.
     *
     * @param query the search query to filter decks, may be null or empty
     * @return a list of deck view models for the current user
     */
    public List<DeckCardViewModel> listDecksForCurrentUser(String query) {
        List<Deck> decks = deckQueryService.listDecksForCurrentUser(query);
        return decks.stream().map(deckQueryService::toViewModel).toList();
    }
}
