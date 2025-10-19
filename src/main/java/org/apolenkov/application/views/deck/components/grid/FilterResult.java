package org.apolenkov.application.views.deck.components.grid;

import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Flashcard;

/**
 * Result of applying filter to flashcards.
 * Contains both filtered flashcards and known card IDs to avoid duplicate queries.
 *
 * @param filteredFlashcards the filtered flashcards based on search and filter criteria
 * @param knownCardIds the set of known card IDs (always loaded for status display)
 */
public record FilterResult(List<Flashcard> filteredFlashcards, Set<Long> knownCardIds) {

    /**
     * Compact constructor for validation.
     */
    public FilterResult {
        if (filteredFlashcards == null) {
            filteredFlashcards = List.of();
        }
        if (knownCardIds == null) {
            knownCardIds = Set.of();
        }
    }
}
