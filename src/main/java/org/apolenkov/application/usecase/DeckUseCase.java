package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;

/**
 * Use case interface for deck management operations.
 *
 * <p>This interface defines the core business operations for managing flashcard decks.
 * It provides a clean abstraction layer between the application services and
 * the underlying data access layer.</p>
 *
 * <p>Key operations include:</p>
 * <ul>
 *   <li>Retrieving decks by various criteria</li>
 *   <li>Creating and updating deck information</li>
 *   <li>Deleting decks and their associated data</li>
 *   <li>User-specific deck management</li>
 * </ul>
 */
public interface DeckUseCase {
    /**
     * Retrieves all decks in the system.
     *
     * @return a list of all available decks
     */
    List<Deck> getAllDecks();

    /**
     * Retrieves all decks belonging to a specific user.
     *
     * @param userId the ID of the user whose decks to retrieve
     * @return a list of decks owned by the specified user
     */
    List<Deck> getDecksByUserId(Long userId);

    /**
     * Retrieves a specific deck by its ID.
     *
     * @param id the unique identifier of the deck
     * @return an Optional containing the deck if found, empty otherwise
     */
    Optional<Deck> getDeckById(Long id);

    /**
     * Saves a deck to the system.
     *
     * <p>If the deck has an ID, it will be updated. If no ID is present,
     * a new deck will be created.</p>
     *
     * @param deck the deck to save
     * @return the saved deck with generated ID if it was new
     */
    Deck saveDeck(Deck deck);

    /**
     * Deletes a deck and all its associated data.
     *
     * @param id the ID of the deck to delete
     */
    void deleteDeck(Long id);
}
