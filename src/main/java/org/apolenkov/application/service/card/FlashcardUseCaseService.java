package org.apolenkov.application.service.card;

import jakarta.validation.Validator;
import org.apolenkov.application.application.usecase.FlashcardUseCase;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Flashcard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@org.springframework.context.annotation.Primary
public class FlashcardUseCaseService implements FlashcardUseCase {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    public FlashcardUseCaseService(DeckRepository deckRepository,
                                   FlashcardRepository flashcardRepository,
                                   Validator validator) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.validator = validator;
    }

    @Override
    public List<Flashcard> getFlashcardsByDeckId(Long deckId) {
        return flashcardRepository.findByDeckId(deckId);
    }

    @Override
    public Optional<Flashcard> getFlashcardById(Long id) {
        return flashcardRepository.findById(id);
    }

    @Override
    @Transactional
    public Flashcard saveFlashcard(Flashcard flashcard) {
        var violations = validator.validate(flashcard);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new IllegalArgumentException("Validation failed: " + message);
        }
        Flashcard saved = flashcardRepository.save(flashcard);
        deckRepository.findById(saved.getDeckId()).ifPresent(deck -> {
            List<Flashcard> deckCards = getFlashcardsByDeckId(deck.getId());
            deck.setFlashcards(deckCards);
            deckRepository.save(deck);
        });
        return saved;
    }

    @Override
    @Transactional
    public void deleteFlashcard(Long id) {
        Optional<Flashcard> flashcard = getFlashcardById(id);
        if (flashcard.isPresent()) {
            Long deckId = flashcard.get().getDeckId();
            flashcardRepository.deleteById(id);
            deckRepository.findById(deckId).ifPresent(deck -> {
                List<Flashcard> deckCards = getFlashcardsByDeckId(deck.getId());
                deck.setFlashcards(deckCards);
                deckRepository.save(deck);
            });
        }
    }

    @Override
    public List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random) {
        List<Flashcard> allCards = new ArrayList<>(getFlashcardsByDeckId(deckId));
        if (random) {
            java.util.Collections.shuffle(allCards);
        }
        return allCards.stream().limit(count).collect(Collectors.toList());
    }
}


