package org.apolenkov.application.views.home;

import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.query.DeckQueryService;
import org.springframework.stereotype.Component;

/** Presenter for HomeView. Delegates filtering/mapping to application services. */
@Component
public class HomePresenter {

    private final DeckQueryService deckQueryService;

    public HomePresenter(DeckQueryService deckQueryService) {
        this.deckQueryService = deckQueryService;
    }

    public List<DeckCardViewModel> listDecksForCurrentUser(String query) {
        List<Deck> decks = deckQueryService.listDecksForCurrentUser(query);
        return decks.stream().map(deckQueryService::toViewModel).toList();
    }
}
