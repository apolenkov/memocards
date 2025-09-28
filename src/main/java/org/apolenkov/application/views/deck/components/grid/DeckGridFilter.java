package org.apolenkov.application.views.deck.components.grid;

import java.util.List;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for filtering flashcards based on search and filter criteria.
 * Provides methods to apply search queries and hide known filters.
 */
public final class DeckGridFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckGridFilter.class);

    private DeckGridFilter() {
        // Utility class
    }

    /**
     * Applies search and filter criteria to the flashcards.
     *
     * @param flashcards the list of flashcards to filter
     * @param searchQuery the search query
     * @param hideKnown whether to hide known cards
     * @param statsService service for statistics tracking
     * @param currentDeckId current deck ID
     * @return filtered list of flashcards
     */
    public static List<Flashcard> applyFilter(
            final List<Flashcard> flashcards,
            final String searchQuery,
            final boolean hideKnown,
            final StatsService statsService,
            final Long currentDeckId) {
        if (flashcards == null || currentDeckId == null) {
            LOGGER.debug(
                    "Cannot apply filter: allFlashcards={}, currentDeckId={}",
                    flashcards != null ? flashcards.size() : "null",
                    currentDeckId);
            return List.of();
        }

        LOGGER.debug(
                "Applying filter: searchQuery='{}', hideKnown={}, totalCards={}",
                searchQuery,
                hideKnown,
                flashcards.size());

        List<Flashcard> filtered = flashcards.stream()
                .filter(card -> searchQuery.isEmpty()
                        || card.getFrontText().toLowerCase().contains(searchQuery)
                        || card.getBackText().toLowerCase().contains(searchQuery))
                .filter(card -> {
                    boolean isKnown = statsService.isCardKnown(currentDeckId, card.getId());
                    return !hideKnown || !isKnown;
                })
                .toList();

        LOGGER.debug("Filter applied: {} cards visible out of {}", filtered.size(), flashcards.size());
        return filtered;
    }
}
