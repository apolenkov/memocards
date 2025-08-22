package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Flashcard;

/**
 * Use case interface for flashcard management operations.
 *
 * <p>This interface defines the core business operations for managing flashcards
 * within decks. It provides a clean abstraction layer between the application
 * services and the underlying data access layer.</p>
 *
 * <p>Key operations include:</p>
 * <ul>
 *   <li>Retrieving flashcards by various criteria</li>
 *   <li>Creating and updating flashcard information</li>
 *   <li>Deleting flashcards</li>
 *   <li>Practice session preparation and card counting</li>
 * </ul>
 */
public interface FlashcardUseCase {
    /**
     * Retrieves all flashcards belonging to a specific deck.
     *
     * @param deckId the ID of the deck to retrieve flashcards for
     * @return a list of all flashcards in the specified deck
     */
    List<Flashcard> getFlashcardsByDeckId(Long deckId);

    /**
     * Retrieves a specific flashcard by its ID.
     *
     * @param id the unique identifier of the flashcard
     * @return an Optional containing the flashcard if found, empty otherwise
     */
    Optional<Flashcard> getFlashcardById(Long id);

    /**
     * Saves a flashcard to the system.
     *
     * <p>If the flashcard has an ID, it will be updated. If no ID is present,
     * a new flashcard will be created.</p>
     *
     * @param flashcard the flashcard to save
     * @return the saved flashcard with generated ID if it was new
     */
    Flashcard saveFlashcard(Flashcard flashcard);

    /**
     * Deletes a flashcard from the system.
     *
     * @param id the ID of the flashcard to delete
     */
    void deleteFlashcard(Long id);

    /**
     * Retrieves flashcards for a practice session.
     *
     * <p>Prepares a subset of flashcards for practice, optionally randomizing
     * the order to provide varied practice experiences.</p>
     *
     * @param deckId the ID of the deck to practice
     * @param count the number of flashcards to retrieve
     * @param random whether to randomize the order of flashcards
     * @return a list of flashcards prepared for practice
     */
    List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random);

    /**
     * Counts the total number of flashcards in a deck.
     *
     * @param deckId the ID of the deck to count flashcards for
     * @return the total number of flashcards in the specified deck
     */
    long countByDeckId(Long deckId);
}
