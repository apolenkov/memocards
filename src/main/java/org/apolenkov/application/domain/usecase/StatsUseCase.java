package org.apolenkov.application.domain.usecase;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.port.StatsRepository.DeckAggregate;

/**
 * Core business operations for statistics and progress tracking.
 */
public interface StatsUseCase {
    /**
     * Records practice session and updates daily statistics.
     *
     * @param sessionData session data containing all required parameters
     * @throws IllegalArgumentException if any parameter violates constraints
     */
    void recordSession(SessionStatsDto sessionData);

    /**
     * Calculates progress percentage for deck based on known cards.
     *
     * @param deckId ID of deck to calculate progress for
     * @param deckSize total number of cards in deck
     * @return progress percentage (0-100)
     */
    int getDeckProgressPercent(long deckId, int deckSize);

    /**
     * Checks if specific card is marked as known in deck.
     *
     * @param deckId ID of deck containing card
     * @param cardId ID of card to check
     * @return true if card is marked as known
     */
    boolean isCardKnown(long deckId, long cardId);

    /**
     * Retrieves all card IDs marked as known in specific deck.
     *
     * @param deckId ID of deck to retrieve known cards for
     * @return set of card IDs marked as known
     */
    Set<Long> getKnownCardIds(long deckId);

    /**
     * Sets knowledge status of specific card in deck.
     *
     * @param deckId ID of deck containing card
     * @param cardId ID of card to update
     * @param known true to mark card as known, false to mark as unknown
     */
    void setCardKnown(long deckId, long cardId, boolean known);

    /**
     * Resets all progress for specific deck.
     *
     * @param deckId ID of deck to reset progress for
     */
    void resetDeckProgress(long deckId);

    /**
     * Retrieves aggregated statistics for multiple decks.
     *
     * @param deckIds list of deck IDs to aggregate statistics for
     * @return map of deck ID to aggregated statistics
     */
    Map<Long, DeckAggregate> getDeckAggregates(List<Long> deckIds);
}
