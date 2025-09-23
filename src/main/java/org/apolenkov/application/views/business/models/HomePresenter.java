package org.apolenkov.application.views.business.models;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.springframework.stereotype.Component;

/**
 * Presenter for the home view, handling deck listing operations.
 */
@Component
public class HomePresenter {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final UserUseCase userUseCase;

    /**
     * Creates a new HomePresenter with the specified dependencies.
     *
     * @param deckUseCaseParam the use case for deck operations (non-null)
     * @param flashcardUseCaseParam the use case for flashcard operations (non-null)
     * @param statsServiceParam the service for statistics operations (non-null)
     * @param userUseCaseParam the use case for user operations (non-null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public HomePresenter(
            final DeckUseCase deckUseCaseParam,
            final FlashcardUseCase flashcardUseCaseParam,
            final StatsService statsServiceParam,
            final UserUseCase userUseCaseParam) {
        if (deckUseCaseParam == null) {
            throw new IllegalArgumentException("DeckUseCase cannot be null");
        }
        if (flashcardUseCaseParam == null) {
            throw new IllegalArgumentException("FlashcardUseCase cannot be null");
        }
        if (statsServiceParam == null) {
            throw new IllegalArgumentException("StatsService cannot be null");
        }
        if (userUseCaseParam == null) {
            throw new IllegalArgumentException("UserUseCase cannot be null");
        }
        this.deckUseCase = deckUseCaseParam;
        this.flashcardUseCase = flashcardUseCaseParam;
        this.statsService = statsServiceParam;
        this.userUseCase = userUseCaseParam;
    }

    /**
     * Lists decks for the current user based on an optional search query.
     *
     * @param query the search query to filter decks, maybe null or empty
     * @return a list of deck view models for the current user, never null (maybe empty)
     */
    public List<DeckCardViewModel> listDecksForCurrentUser(final String query) {
        // Get current user ID and load all their decks
        long userId = userUseCase.getCurrentUser().getId();
        List<Deck> decks = deckUseCase.getDecksByUserId(userId);

        // Normalize search query: convert to lowercase, trim whitespace, handle null
        String normalized = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";

        // Apply text filtering if query is provided
        if (!normalized.isEmpty()) {
            decks = decks.stream()
                    .filter(d -> contains(d.getTitle(), normalized) || contains(d.getDescription(), normalized))
                    .toList();
        }

        // Sort decks alphabetically by title, handling null titles gracefully
        decks = decks.stream()
                .sorted(Comparator.comparing(Deck::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return decks.stream().map(this::toViewModel).toList();
    }

    /**
     * Converts a deck entity to a view model for UI display.
     *
     * @param deck the deck entity to convert
     * @return a DeckCardViewModel with deck data and progress statistics
     */
    private DeckCardViewModel toViewModel(final Deck deck) {
        // Calculate deck size by counting flashcards
        int deckSize = (int) flashcardUseCase.countByDeckId(deck.getId());
        // Count cards marked as known by the user
        int known = statsService.getKnownCardIds(deck.getId()).size();
        // Calculate progress percentage based on known cards vs. total
        int percent = statsService.getDeckProgressPercent(deck.getId(), deckSize);
        return new DeckCardViewModel(deck.getId(), deck.getTitle(), deck.getDescription(), deckSize, known, percent);
    }

    /**
     * Checks if a string value contains the specified query text.
     *
     * @param value the string to search in (can be null)
     * @param query the query text to search for
     * @return true if the value contains the query, false otherwise
     */
    private static boolean contains(final String value, final String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
