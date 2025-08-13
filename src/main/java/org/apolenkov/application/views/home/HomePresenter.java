package org.apolenkov.application.views.home;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;

/** Presenter for HomeView. Contains filtering and mapping logic. */
@org.springframework.stereotype.Component
public class HomePresenter {

    private final DeckUseCase deckUseCase;
    private final UserUseCase userUseCase;
    private final StatsService statsService;
    private final FlashcardUseCase flashcardUseCase;

    public HomePresenter(
            DeckUseCase deckUseCase,
            UserUseCase userUseCase,
            StatsService statsService,
            FlashcardUseCase flashcardUseCase) {
        this.deckUseCase = deckUseCase;
        this.userUseCase = userUseCase;
        this.statsService = statsService;
        this.flashcardUseCase = flashcardUseCase;
    }

    public List<DeckCardViewModel> listDecksForCurrentUser(String query) {
        Long userId = userUseCase.getCurrentUser().getId();
        List<Deck> decks = deckUseCase.getDecksByUserId(userId);

        String normalized = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";
        if (!normalized.isEmpty()) {
            decks = decks.stream()
                    .filter(d -> (d.getTitle() != null
                                    && d.getTitle().toLowerCase(Locale.ROOT).contains(normalized))
                            || (d.getDescription() != null
                                    && d.getDescription()
                                            .toLowerCase(Locale.ROOT)
                                            .contains(normalized)))
                    .collect(Collectors.toList());
        }

        return decks.stream()
                .sorted(Comparator.comparing(Deck::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toViewModel)
                .collect(Collectors.toList());
    }

    public DeckCardViewModel toViewModel(Deck deck) {
        int deckSize = flashcardUseCase.getFlashcardsByDeckId(deck.getId()).size();
        int known = statsService.getKnownCardIds(deck.getId()).size();
        int percent = statsService.getDeckProgressPercent(deck.getId(), deckSize);
        return new DeckCardViewModel(deck.getId(), deck.getTitle(), deck.getDescription(), deckSize, known, percent);
    }
}
