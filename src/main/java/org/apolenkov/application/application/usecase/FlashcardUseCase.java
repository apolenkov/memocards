package org.apolenkov.application.application.usecase;

import org.apolenkov.application.model.Flashcard;

import java.util.List;
import java.util.Optional;

public interface FlashcardUseCase {
    List<Flashcard> getFlashcardsByDeckId(Long deckId);
    Optional<Flashcard> getFlashcardById(Long id);
    Flashcard saveFlashcard(Flashcard flashcard);
    void deleteFlashcard(Long id);
    List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random);
}


