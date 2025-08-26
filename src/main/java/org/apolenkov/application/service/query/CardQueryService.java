package org.apolenkov.application.service.query;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-level query service for flashcards.
 * This service centralizes search, filtering, and known-cards logic for the UI layer.
 * It provides efficient methods for querying flashcards with various filtering options
 * including text search and knowledge status filtering.
 */
@Service
public class CardQueryService {

    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;

    /**
     * Creates a new CardQueryService with required dependencies.
     *
     * @param flashcardUseCase service for flashcard operations
     * @param statsService service for statistics and progress tracking
     */
    public CardQueryService(FlashcardUseCase flashcardUseCase, StatsService statsService) {
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
    }

    /**
     * Checks if a string value contains the specified query text (case-insensitive, null-safe).
     *
     * @param value the string to search in (can be null)
     * @param query the query text to search for
     * @return true if the value contains the query, false otherwise
     */
    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    /**
     * Gets filtered flashcards for a specific deck with search and knowledge status filtering.
     *
     * @param deckId the ID of the deck to search in
     * @param query the search query text (can be null for no filtering)
     * @param hideKnown whether to exclude cards marked as known
     * @return a filtered list of flashcards matching the criteria
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("java:S6809")
    public List<Flashcard> listFilteredFlashcards(long deckId, String query, boolean hideKnown) {
        List<Flashcard> all = flashcardUseCase.getFlashcardsByDeckId(deckId);
        Set<Long> known = statsService.getKnownCardIds(deckId);
        return filterFlashcards(all, query, known, hideKnown);
    }

    /**
     * Filters a list of flashcards based on search criteria and knowledge status.
     *
     * @param base the base list of flashcards to filter
     * @param query the search query text (can be null for no filtering)
     * @param knownIds set of card IDs that are marked as known
     * @param hideKnown whether to exclude known cards from results
     * @return a filtered list of flashcards matching the criteria
     */
    @Transactional(readOnly = true)
    public List<Flashcard> filterFlashcards(List<Flashcard> base, String query, Set<Long> knownIds, boolean hideKnown) {
        // Normalize query: convert to lowercase, trim whitespace, handle null
        String q = query != null ? query.toLowerCase(Locale.ROOT).trim() : "";

        return base.stream()
                // Apply text search across multiple card fields (empty query matches all)
                .filter(fc -> q.isEmpty()
                        || contains(fc.getFrontText(), q)
                        || contains(fc.getBackText(), q)
                        || contains(fc.getExample(), q))
                // Optionally filter out known cards based on hideKnown flag
                .filter(fc -> !hideKnown || !knownIds.contains(fc.getId()))
                .toList();
    }
}
