package org.apolenkov.application.application.usecase;

import org.apolenkov.application.model.Deck;

import java.util.List;
import java.util.Optional;

public interface DeckUseCase {
    List<Deck> getAllDecks();
    List<Deck> getDecksByUserId(Long userId);
    Optional<Deck> getDeckById(Long id);
    Deck saveDeck(Deck deck);
    void deleteDeck(Long id);
}


