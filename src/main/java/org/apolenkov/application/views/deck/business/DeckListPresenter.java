package org.apolenkov.application.views.deck.business;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.stats.StatsService;
import org.springframework.stereotype.Component;

/**
 * Presenter for the home view, handling deck listing operations.
 */
@Component
public class DeckListPresenter {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final UserUseCase userUseCase;

    /**
     * Creates a new DeckListPresenter with the specified dependencies.
     *
     * @param deckUseCaseParam the use case for deck operations (non-null)
     * @param flashcardUseCaseParam the use case for flashcard operations (non-null)
     * @param statsServiceParam the service for statistics operations (non-null)
     * @param userUseCaseParam the use case for user operations (non-null)
     * @throws IllegalArgumentException if any parameter is null
     */
    public DeckListPresenter(
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

        // Step 1: Batch load flashcard counts for all decks in single query
        List<Long> deckIds = decks.stream().map(Deck::getId).toList();
        Map<Long, Long> deckSizes = flashcardUseCase.countByDeckIds(deckIds);

        // Step 2: Batch load known card IDs for all decks in single query
        Map<Long, Set<Long>> knownCardsByDeck = statsService.getKnownCardIdsBatch(deckIds);

        return decks.stream()
                .map(deck -> toViewModel(deck, deckSizes, knownCardsByDeck))
                .toList();
    }

    /**
     * Converts a deck entity to a view model for UI display.
     * Uses pre-loaded data maps to avoid repeated database queries.
     *
     * @param deck the deck entity to convert
     * @param deckSizes pre-loaded map of deck ID to flashcard count
     * @param knownCardsByDeck pre-loaded map of deck ID to known card IDs
     * @return a DeckCardViewModel with deck data and progress statistics
     */
    private DeckCardViewModel toViewModel(
            final Deck deck, final Map<Long, Long> deckSizes, final Map<Long, Set<Long>> knownCardsByDeck) {
        // Get deck size from pre-loaded map (no database query)
        int deckSize = deckSizes.getOrDefault(deck.getId(), 0L).intValue();

        // Get known cards count from pre-loaded map (no database query)
        Set<Long> knownCards = knownCardsByDeck.getOrDefault(deck.getId(), Set.of());
        int known = knownCards.size();

        // Calculate progress percentage inline (no additional query)
        int percent = deckSize > 0 ? (int) Math.round(100.0 * known / deckSize) : 0;
        percent = Math.clamp(percent, 0, 100);

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
