package org.apolenkov.application.domain.port;

import org.apolenkov.application.model.Deck;

import java.util.List;
import java.util.Optional;

/**
 * Port for accessing and mutating Deck aggregates.
 * Clean Architecture: implemented by infrastructure adapters.
 */
public interface DeckRepository {
    List<Deck> findAll();

    List<Deck> findByUserId(Long userId);

    Optional<Deck> findById(Long id);

    Deck save(Deck deck);

    void deleteById(Long id);
}


