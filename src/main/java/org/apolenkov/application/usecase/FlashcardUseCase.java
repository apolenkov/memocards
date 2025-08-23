package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Flashcard;

/**
 * Use case interface for flashcard management operations.
 *
 * <p>Defines core business operations for managing flashcards within decks.
 * Provides clean abstraction layer between application services and
 * underlying data access layer.</p>
 *
 * <p>Key operations:</p>
 * <ul>
 *   <li>Retrieving flashcards by various criteria</li>
 *   <li>Creating and updating flashcard information</li>
 *   <li>Deleting flashcards</li>
 *   <li>Practice session preparation and card counting</li>
 * </ul>
 */
public interface FlashcardUseCase {
    /**
     * Gets all flashcards belonging to specific deck.
     *
     * @param deckId ID of deck to retrieve flashcards for
     * @return list of all flashcards in specified deck
     */
    List<Flashcard> getFlashcardsByDeckId(Long deckId);

    /**
     * Gets specific flashcard by ID.
     *
     * @param id unique identifier of flashcard
     * @return Optional containing flashcard if found, empty otherwise
     */
    Optional<Flashcard> getFlashcardById(Long id);

    /**
     * Saves flashcard to system.
     *
     * <p>If flashcard has ID, it will be updated. If no ID is present,
     * new flashcard will be created.</p>
     *
     * @param flashcard flashcard to save
     * @return saved flashcard with generated ID if it was new
     */
    Flashcard saveFlashcard(Flashcard flashcard);

    /**
     * Deletes flashcard from system.
     *
     * @param id ID of flashcard to delete
     */
    void deleteFlashcard(Long id);

    /**
     * Gets flashcards for practice session.
     *
     * <p>Prepares subset of flashcards for practice, optionally randomizing
     * order to provide varied practice experiences.</p>
     *
     * @param deckId ID of deck to practice
     * @param count number of flashcards to retrieve
     * @param random whether to randomize order of flashcards
     * @return list of flashcards prepared for practice
     */
    List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random);

    /**
     * Counts total number of flashcards in deck.
     *
     * @param deckId ID of deck to count flashcards for
     * @return total number of flashcards in specified deck
     */
    long countByDeckId(Long deckId);
}
