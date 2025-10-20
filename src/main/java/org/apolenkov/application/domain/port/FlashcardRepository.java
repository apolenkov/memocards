package org.apolenkov.application.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.model.Flashcard;
import org.springframework.data.domain.Pageable;

/**
 * Domain port for flashcard management operations.
 *
 * <p>Defines contract for CRUD operations on flashcards,
 * including deck-specific queries and bulk operations.</p>
 */
public interface FlashcardRepository {

    /**
     * Finds all flashcards in specific deck.
     *
     * @param deckId deck identifier
     * @return list of flashcards in deck
     */
    List<Flashcard> findByDeckId(long deckId);

    /**
     * Finds flashcard by identifier.
     *
     * @param id flashcard identifier
     * @return flashcard if found, empty otherwise
     */
    Optional<Flashcard> findById(long id);

    /**
     * Saves flashcard (creates new or updates existing).
     *
     * @param flashcard flashcard to save
     */
    void save(Flashcard flashcard);

    /**
     * Deletes flashcard by identifier.
     *
     * @param id flashcard identifier to delete
     */
    void deleteById(long id);

    /**
     * Counts number of flashcards in deck.
     *
     * @param deckId deck identifier
     * @return total number of flashcards in deck
     */
    long countByDeckId(long deckId);

    /**
     * Counts flashcards for multiple decks in single query.
     *
     * @param deckIds collection of deck identifiers (non-null, may be empty)
     * @return map of deck ID to flashcard count (non-null, contains only decks with flashcards)
     */
    Map<Long, Long> countByDeckIds(Collection<Long> deckIds);

    /**
     * Finds flashcards using dynamic filtering.
     * Supports combinations of search query and known/unknown status.
     *
     * @param deckId deck identifier
     * @param searchQuery search query (can be null or empty)
     * @param filterOption filter option for known/unknown status
     * @param pageable pagination and sorting parameters
     * @return list of flashcards matching criteria
     */
    List<Flashcard> findFlashcardsWithFilter(
            long deckId, String searchQuery, FilterOption filterOption, Pageable pageable);

    /**
     * Counts flashcards using dynamic filtering.
     * Supports combinations of search query and known/unknown status.
     *
     * @param deckId deck identifier
     * @param searchQuery search query (can be null or empty)
     * @param filterOption filter option for known/unknown status
     * @return count of flashcards matching criteria
     */
    long countFlashcardsWithFilter(long deckId, String searchQuery, FilterOption filterOption);

    /**
     * Deletes all flashcards in specific deck.
     *
     * @param deckId deck identifier
     */
    void deleteByDeckId(long deckId);
}
