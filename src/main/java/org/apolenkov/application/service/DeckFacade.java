package org.apolenkov.application.service;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeckFacade {

    private final DeckUseCase deckUseCase;
    private final FlashcardUseCase flashcardUseCase;
    private final StatsService statsService;
    private final Validator validator;

    public DeckFacade(
            DeckUseCase deckUseCase,
            FlashcardUseCase flashcardUseCase,
            StatsService statsService,
            Validator validator) {
        this.deckUseCase = deckUseCase;
        this.flashcardUseCase = flashcardUseCase;
        this.statsService = statsService;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public Deck getDeckOrThrow(long deckId) {
        return deckUseCase.getDeckById(deckId).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<Flashcard> loadFlashcards(long deckId) {
        return flashcardUseCase.getFlashcardsByDeckId(deckId);
    }

    @Transactional(readOnly = true)
    public Set<Long> getKnown(long deckId) {
        return statsService.getKnownCardIds(deckId);
    }

    @Transactional
    public void toggleKnown(long deckId, long cardId) {
        boolean known = statsService.isCardKnown(deckId, cardId);
        statsService.setCardKnown(deckId, cardId, !known);
    }

    @Transactional
    public void resetProgress(long deckId) {
        statsService.resetDeckProgress(deckId);
    }

    @Transactional(readOnly = true)
    public int deckSize(long deckId) {
        return loadFlashcards(deckId).size();
    }

    @Transactional(readOnly = true)
    public int progressPercent(long deckId) {
        return statsService.getDeckProgressPercent(deckId, deckSize(deckId));
    }

    @Transactional
    public Flashcard saveFlashcard(Flashcard flashcard) {
        return flashcardUseCase.saveFlashcard(flashcard);
    }

    @Transactional
    public void deleteFlashcard(Long id) {
        flashcardUseCase.deleteFlashcard(id);
    }

    @Transactional
    public Deck saveDeck(Deck deck) {
        return deckUseCase.saveDeck(deck);
    }

    @Transactional
    public void deleteDeck(Long deckId) {
        deckUseCase.deleteDeck(deckId);
    }

}
