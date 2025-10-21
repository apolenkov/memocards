package org.apolenkov.application.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.model.Card;
import org.springframework.data.domain.Pageable;

/**
 * Domain port for card management operations.
 *
 * <p>Defines contract for CRUD operations on cards,
 * including deck-specific queries and bulk operations.</p>
 */
public interface CardRepository {

    /**
     * Finds all cards in specific deck.
     *
     * @param deckId deck identifier
     * @return list of cards in deck
     */
    List<Card> findByDeckId(long deckId);

    /**
     * Finds card by identifier.
     *
     * @param id card identifier
     * @return card if found, empty otherwise
     */
    Optional<Card> findById(long id);

    /**
     * Saves card (creates new or updates existing).
     *
     * @param card card to save
     */
    void save(Card card);

    /**
     * Deletes card by identifier.
     *
     * @param id card identifier to delete
     */
    void deleteById(long id);

    /**
     * Counts number of cards in deck.
     *
     * @param deckId deck identifier
     * @return total number of cards in deck
     */
    long countByDeckId(long deckId);

    /**
     * Counts cards for multiple decks in single query.
     *
     * @param deckIds collection of deck identifiers (non-null, may be empty)
     * @return map of deck ID to card count (non-null, contains only decks with cards)
     */
    Map<Long, Long> countByDeckIds(Collection<Long> deckIds);

    /**
     * Finds cards using dynamic filtering.
     * Supports combinations of search query and known/unknown status.
     *
     * @param deckId deck identifier
     * @param searchQuery search query (can be null or empty)
     * @param filterOption filter option for known/unknown status
     * @param pageable pagination and sorting parameters
     * @return list of cards matching criteria
     */
    List<Card> findCardsWithFilter(long deckId, String searchQuery, FilterOption filterOption, Pageable pageable);

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

    /**
     * Deletes all cards in specific deck.
     *
     * @param deckId deck identifier
     */
    void deleteByDeckId(long deckId);
}
