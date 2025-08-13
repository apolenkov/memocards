package org.apolenkov.application.domain.port;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Flashcard;

/** Port for accessing and mutating Flashcard entities. */
public interface FlashcardRepository {
    List<Flashcard> findByDeckId(Long deckId);

    Optional<Flashcard> findById(Long id);

    Flashcard save(Flashcard flashcard);

    void deleteById(Long id);
}
