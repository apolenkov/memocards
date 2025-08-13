package org.apolenkov.application.views.presenter;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeckPresenter {

    private final DeckUseCase deckUseCase;
    private final StatsService statsService;
    private final DeckFacade deckFacade;

    public DeckPresenter(DeckUseCase deckUseCase, StatsService statsService, DeckFacade deckFacade) {
        this.deckUseCase = deckUseCase;
        this.statsService = statsService;
        this.deckFacade = deckFacade;
    }

    public Optional<Deck> loadDeck(long deckId) {
        return deckUseCase.getDeckById(deckId);
    }

    public List<Flashcard> loadFlashcards(long deckId) {
        return deckFacade.loadFlashcards(deckId);
    }

    public List<Flashcard> filterFlashcards(List<Flashcard> base, String query, Set<Long> knownIds, boolean hideKnown) {
        String q = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";
        return base.stream()
                .filter(fc -> q.isEmpty()
                        || (fc.getFrontText() != null
                                && fc.getFrontText().toLowerCase(Locale.ROOT).contains(q))
                        || (fc.getBackText() != null
                                && fc.getBackText().toLowerCase(Locale.ROOT).contains(q))
                        || (fc.getExample() != null
                                && fc.getExample().toLowerCase(Locale.ROOT).contains(q)))
                .filter(fc -> !hideKnown || !knownIds.contains(fc.getId()))
                .collect(Collectors.toList());
    }

    /** High-level query for UI: returns flashcards already filtered by search and known-status. */
    public List<Flashcard> listFilteredFlashcards(long deckId, String rawQuery, boolean hideKnown) {
        List<Flashcard> all = loadFlashcards(deckId);
        Set<Long> known = getKnown(deckId);
        return filterFlashcards(all, rawQuery, known, hideKnown);
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
