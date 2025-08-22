package org.apolenkov.application.service.query;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.home.DeckCardViewModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-level query service for decks.
 *
 * <p>This service provides search functionality and view model creation for decks.
 * It centralizes deck-related query operations including text search, sorting,
 * and the creation of view models for UI components.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Text-based search across deck titles and descriptions</li>
 *   <li>User-specific deck filtering and sorting</li>
 *   <li>View model creation with progress statistics</li>
 *   <li>Transactional read-only operations for performance</li>
 * </ul>
 */
@Service
public class DeckQueryService {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final UserUseCase userUseCase;

    /**
     * Constructs a new DeckQueryService with required dependencies.
     *
     * @param deckUseCase service for deck operations
     * @param flashcardUseCase service for flashcard operations
     * @param statsService service for statistics and progress tracking
     * @param userUseCase service for user operations
     */
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

    /**
     * Retrieves and filters decks for the current user.
     *
     * <p>Loads all decks belonging to the current user and applies optional
     * text-based filtering. Results are sorted alphabetically by title with
     * null titles handled gracefully.</p>
     *
     * @param query optional search query for filtering deck titles and descriptions
     * @return a sorted list of user's decks, optionally filtered by search query
     */
    @Transactional(readOnly = true)
    public List<Deck> listDecksForCurrentUser(String query) {
        // Get current user ID and load all their decks
        Long userId = userUseCase.getCurrentUser().getId();
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
        return decks.stream()
                .sorted(Comparator.comparing(Deck::getTitle, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    /**
     * Converts a deck entity to a view model for UI display.
     *
     * <p>Creates a comprehensive view model containing deck information and
     * calculated progress statistics. The method aggregates data from multiple
     * sources to provide a complete picture for UI components.</p>
     *
     * @param deck the deck entity to convert
     * @return a DeckCardViewModel with deck data and progress statistics
     */
    @Transactional(readOnly = true)
    public DeckCardViewModel toViewModel(Deck deck) {
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
     * <p>Performs a case-insensitive substring search with null safety.
     * Returns false if the value is null, otherwise checks if the query
     * is contained within the value after converting both to lowercase.</p>
     *
     * @param value the string to search in (can be null)
     * @param query the query text to search for
     * @return true if the value contains the query, false otherwise
     */
    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
