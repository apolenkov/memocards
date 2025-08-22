package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;

/**
 * Repository interface for managing deck entities.
 *
 * <p>This port defines the contract for deck data access operations
 * following Clean Architecture principles. Infrastructure adapters
 * implement this interface to provide concrete data access mechanisms.</p>
 *
 */
public interface DeckRepository {

    /**
     * Retrieves all decks from the repository.
     *
     * @return a list of all decks
     */
    List<Deck> findAll();

    /**
     * Retrieves all decks belonging to a specific user.
     *
     * @param userId the ID of the user whose decks to retrieve
     * @return a list of decks belonging to the specified user
     */
    List<Deck> findByUserId(Long userId);

    /**
     * Retrieves a deck by its unique identifier.
     *
     * @param id the unique identifier of the deck
     * @return an optional containing the deck if found, empty otherwise
     */
    Optional<Deck> findById(Long id);

    /**
     * Saves a deck to the repository.
     *
     * @param deck the deck to save
     * @return the saved deck, potentially with updated fields
     */
    Deck save(Deck deck);

    /**
     * Deletes a deck from the repository by its ID.
     *
     * @param id the unique identifier of the deck to delete
     */
    void deleteById(Long id);
}
