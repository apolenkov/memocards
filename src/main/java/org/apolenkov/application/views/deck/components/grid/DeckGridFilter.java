package org.apolenkov.application.views.deck.components.grid;

import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.StatsService;
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
     * Returns both filtered flashcards and known card IDs to avoid duplicate queries.
     *
     * @param flashcards the list of flashcards to filter
     * @param searchQuery the search query
     * @param hideKnown whether to hide known cards
     * @param statsService service for statistics tracking
     * @param currentDeckId current deck ID
     * @return FilterResult containing filtered flashcards and known card IDs
     */
    public static FilterResult applyFilter(
            final List<Flashcard> flashcards,
            final String searchQuery,
            final boolean hideKnown,
            final StatsService statsService,
            final Long currentDeckId) {
        // Early return if not ready (normal during component initialization)
        if (flashcards == null || flashcards.isEmpty() || currentDeckId == null) {
            // Silent skip - this is expected during initialization lifecycle
            return new FilterResult(List.of(), Set.of());
        }

        // Load known card IDs once (will be reused by grid for status column)
        // Always load known card IDs to show correct status in UI
        Set<Long> knownCardIds = statsService.getKnownCardIds(currentDeckId);

        List<Flashcard> filtered = flashcards.stream()
                .filter(card -> searchQuery.isEmpty()
                        || card.getFrontText().toLowerCase().contains(searchQuery)
                        || card.getBackText().toLowerCase().contains(searchQuery))
                .filter(card -> !hideKnown || !knownCardIds.contains(card.getId()))
                .toList();

        // Log only anomalies (all cards filtered out)
        if (filtered.isEmpty() && !flashcards.isEmpty()) {
            LOGGER.debug(
                    "Filter excluded all {} cards: query='{}', hideKnown={}",
                    flashcards.size(),
                    searchQuery,
                    hideKnown);
        }
        return new FilterResult(filtered, knownCardIds);
    }
}
