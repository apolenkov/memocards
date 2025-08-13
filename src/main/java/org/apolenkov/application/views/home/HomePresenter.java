package org.apolenkov.application.views.home;

import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.FlashcardService;
import org.apolenkov.application.service.StatsService;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter for HomeView. Contains filtering and mapping logic.
 */
public class HomePresenter {

    private final FlashcardService flashcardService;
    private final StatsService statsService;

    public HomePresenter(FlashcardService flashcardService, StatsService statsService) {
        this.flashcardService = flashcardService;
        this.statsService = statsService;
    }

    public List<DeckCardViewModel> listDecksForCurrentUser(String query) {
        Long userId = flashcardService.getCurrentUser().getId();
        List<Deck> decks = flashcardService.getDecksByUserId(userId);

        String normalized = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";
        if (!normalized.isEmpty()) {
            decks = decks.stream()
                    .filter(d -> (d.getTitle() != null && d.getTitle().toLowerCase(Locale.ROOT).contains(normalized))
                            || (d.getDescription() != null && d.getDescription().toLowerCase(Locale.ROOT).contains(normalized)))
                    .collect(Collectors.toList());
        }

        return decks.stream()
                .sorted(Comparator.comparing(Deck::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toViewModel)
                .collect(Collectors.toList());
    }

    public DeckCardViewModel toViewModel(Deck deck) {
        int deckSize = deck.getFlashcardCount();
        int known = statsService.getKnownCardIds(deck.getId()).size();
        int percent = statsService.getDeckProgressPercent(deck.getId(), deckSize);
        return new DeckCardViewModel(
                deck.getId(),
                deck.getTitle(),
                deck.getDescription(),
                deckSize,
                known,
                percent
        );
    }
}


