package org.apolenkov.application.views.presenter;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.service.query.CardQueryService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeckPresenter {

    private final DeckUseCase deckUseCase;
    private final StatsService statsService;
    private final DeckFacade deckFacade;
    private final CardQueryService cardQueryService;

    public DeckPresenter(
            DeckUseCase deckUseCase,
            StatsService statsService,
            DeckFacade deckFacade,
            CardQueryService cardQueryService) {
        this.deckUseCase = deckUseCase;
        this.statsService = statsService;
        this.deckFacade = deckFacade;
        this.cardQueryService = cardQueryService;
    }

    public Optional<Deck> loadDeck(long deckId) {
        return deckUseCase.getDeckById(deckId);
    }

    public List<Flashcard> loadFlashcards(long deckId) {
        return deckFacade.loadFlashcards(deckId);
    }

    public List<Flashcard> filterFlashcards(List<Flashcard> base, String query, Set<Long> knownIds, boolean hideKnown) {
        return cardQueryService.filterFlashcards(base, query, knownIds, hideKnown);
    }

    /** High-level query for UI: returns flashcards already filtered by search and known-status. */
    public List<Flashcard> listFilteredFlashcards(long deckId, String rawQuery, boolean hideKnown) {
        return cardQueryService.listFilteredFlashcards(deckId, rawQuery, hideKnown);
    }

    public Set<Long> getKnown(long deckId) {
        return deckFacade.getKnown(deckId);
    }

    public boolean isKnown(long deckId, long cardId) {
        return statsService.isCardKnown(deckId, cardId);
    }

    public void toggleKnown(long deckId, long cardId) {
        deckFacade.toggleKnown(deckId, cardId);
    }

    public void resetProgress(long deckId) {
        deckFacade.resetProgress(deckId);
    }

    public int deckSize(long deckId) {
        return deckFacade.deckSize(deckId);
    }

    public int knownCount(long deckId) {
        return getKnown(deckId).size();
    }

    public int progressPercent(long deckId) {
        return deckFacade.progressPercent(deckId);
    }
}
