package org.apolenkov.application.service.deck;

import jakarta.validation.Validator;
import org.apolenkov.application.application.usecase.DeckUseCase;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@org.springframework.context.annotation.Primary
public class DeckUseCaseService implements DeckUseCase {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    public DeckUseCaseService(DeckRepository deckRepository,
                              FlashcardRepository flashcardRepository,
                              Validator validator) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.validator = validator;
    }

    @Override
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    @Override
    public List<Deck> getDecksByUserId(Long userId) {
        return deckRepository.findByUserId(userId);
    }

    @Override
    public Optional<Deck> getDeckById(Long id) {
        return deckRepository.findById(id);
    }

    @Override
    @Transactional
    public Deck saveDeck(Deck deck) {
        var violations = validator.validate(deck);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new IllegalArgumentException("Validation failed: " + message);
        }
        Deck saved = deckRepository.save(deck);
        List<Flashcard> deckCards = flashcardRepository.findByDeckId(saved.getId());
        saved.setFlashcards(deckCards);
        return saved;
    }

    @Override
    @Transactional
    public void deleteDeck(Long id) {
        flashcardRepository.findByDeckId(id).forEach(card -> flashcardRepository.deleteById(card.getId()));
        deckRepository.deleteById(id);
    }
}


