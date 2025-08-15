package org.apolenkov.application.service.query;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.home.DeckCardViewModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-level query service for decks. Provides search and view models.
 */
@Service
public class DeckQueryService {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final UserUseCase userUseCase;

    public DeckQueryService(
            DeckUseCase deckUseCase,
            FlashcardUseCase flashcardUseCase,
            StatsService statsService,
            UserUseCase userUseCase) {
        this.deckUseCase = deckUseCase;
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
        this.userUseCase = userUseCase;
    }

    @Transactional(readOnly = true)
    public List<Deck> listDecksForCurrentUser(String query) {
        Long userId = userUseCase.getCurrentUser().getId();
        List<Deck> decks = deckUseCase.getDecksByUserId(userId);
        String normalized = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";
        if (!normalized.isEmpty()) {
            decks = decks.stream()
                    .filter(d -> contains(d.getTitle(), normalized) || contains(d.getDescription(), normalized))
                    .collect(Collectors.toList());
        }
        return decks.stream()
                .sorted(Comparator.comparing(Deck::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Transactional(readOnly = true)
    public DeckCardViewModel toViewModel(Deck deck) {
        int deckSize = (int) flashcardUseCase.countByDeckId(deck.getId());
        int known = statsService.getKnownCardIds(deck.getId()).size();
        int percent = statsService.getDeckProgressPercent(deck.getId(), deckSize);
        return new DeckCardViewModel(deck.getId(), deck.getTitle(), deck.getDescription(), deckSize, known, percent);
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
