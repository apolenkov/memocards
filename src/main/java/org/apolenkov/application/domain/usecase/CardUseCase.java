package org.apolenkov.application.domain.usecase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.model.Card;
import org.springframework.data.domain.Pageable;

/**
 * Core business operations for managing cards.
 */
public interface CardUseCase {
    /**
     * Returns cards belonging to specific deck.
     *
     * @param deckId ID of deck to retrieve cards for
     * @return list of all cards in specified deck
     */
    List<Card> getCardsByDeckId(long deckId);

    /**
     * Saves card to system (creates new or updates existing).
     *
     * @param card card to save
     */
    void saveCard(Card card);

    /**
     * Deletes card from system.
     *
     * @param id ID of card to delete
     */
    void deleteCard(long id);

    /**
     * Returns total number of cards in deck.
     *
     * @param deckId ID of deck to count cards for
     * @return total number of cards in specified deck
     */
    long countByDeckId(long deckId);

    /**
     * Returns card counts for multiple decks in single operation.
     *
     * @param deckIds collection of deck IDs to count cards for
     * @return map of deck ID to card count (decks with zero cards may be absent)
     */
    Map<Long, Long> countByDeckIds(Collection<Long> deckIds);

    /**
     * Finds cards using dynamic filtering.
     * Supports combinations of search query and known/unknown status.
     *
     * @param deckId deck identifier
     * @param searchQuery search query (can be null or empty)
     * @param filterOption filter option for known/unknown status
     * @param pageable pagination parameters
     * @return list of cards matching criteria
     */
    List<Card> getCardsWithFilter(long deckId, String searchQuery, FilterOption filterOption, Pageable pageable);

    /**
     * Counts cards using dynamic filtering.
     * Supports combinations of search query and known/unknown status.
     *
     * @param deckId deck identifier
     * @param searchQuery search query (can be null or empty)
     * @param filterOption filter option for known/unknown status
     * @return count of cards matching criteria
     */
    long countCardsWithFilter(long deckId, String searchQuery, FilterOption filterOption);
}
