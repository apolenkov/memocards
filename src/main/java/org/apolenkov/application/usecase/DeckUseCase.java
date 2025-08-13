package org.apolenkov.application.usecase;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;

public interface DeckUseCase {
    List<Deck> getAllDecks();

    List<Deck> getDecksByUserId(Long userId);

    Optional<Deck> getDeckById(Long id);

    Deck saveDeck(Deck deck);

    void deleteDeck(Long id);
}
