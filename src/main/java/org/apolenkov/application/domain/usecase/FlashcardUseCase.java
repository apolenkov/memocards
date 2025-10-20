package org.apolenkov.application.domain.usecase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apolenkov.application.domain.model.FilterOption;
import org.apolenkov.application.model.Flashcard;
import org.springframework.data.domain.Pageable;

/**
 * Core business operations for managing flashcards.
 */
public interface FlashcardUseCase {
    /**
     * Returns flashcards belonging to specific deck.
     *
     * @param deckId ID of deck to retrieve flashcards for
     * @return list of all flashcards in specified deck
     */
    List<Flashcard> getFlashcardsByDeckId(long deckId);

    /**
     * Saves flashcard to system (creates new or updates existing).
     *
     * @param flashcard flashcard to save
     */
    void saveFlashcard(Flashcard flashcard);

    /**
     * Deletes flashcard from system.
     *
     * @param id ID of flashcard to delete
     */
    void deleteFlashcard(long id);

    /**
     * Returns total number of flashcards in deck.
     *
     * @param deckId ID of deck to count flashcards for
     * @return total number of flashcards in specified deck
     */
    long countByDeckId(long deckId);

    /**
     * Returns flashcard counts for multiple decks in single operation.
     *
     * @param deckIds collection of deck IDs to count flashcards for
     * @return map of deck ID to flashcard count (decks with zero flashcards may be absent)
     */
    Map<Long, Long> countByDeckIds(Collection<Long> deckIds);

    /**
     * Finds flashcards using dynamic filtering.
     * Supports combinations of search query and known/unknown status.
     *
     * @param deckId deck identifier
     * @param searchQuery search query (can be null or empty)
     * @param filterOption filter option for known/unknown status
     * @param pageable pagination parameters
     * @return list of flashcards matching criteria
     */
    List<Flashcard> getFlashcardsWithFilter(
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
}
