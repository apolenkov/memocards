package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;

/**
 * Repository interface for managing deck entities.
 *
 * <p>Defines contract for deck data access operations following Clean Architecture principles.
 * Infrastructure adapters implement this interface to provide concrete data access mechanisms.</p>
 */
public interface DeckRepository {

    /**
     * Gets all decks from repository.
     *
     * @return list of all decks
     */
    List<Deck> findAll();

    /**
     * Gets all decks belonging to specific user.
     *
     * @param userId ID of user whose decks to retrieve
     * @return list of decks belonging to specified user
     */
    List<Deck> findByUserId(Long userId);

    /**
     * Gets deck by unique identifier.
     *
     * @param id unique identifier of deck
     * @return optional containing deck if found, empty otherwise
     */
    Optional<Deck> findById(Long id);

    /**
     * Saves deck to repository.
     *
     * @param deck deck to save
     * @return saved deck, potentially with updated fields
     */
    Deck save(Deck deck);

    /**
     * Deletes deck from repository by ID.
     *
     * @param id unique identifier of deck to delete
     */
    void deleteById(Long id);
}
