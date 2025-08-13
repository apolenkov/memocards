package org.apolenkov.application.service.deck;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeckUseCaseService implements DeckUseCase {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    public DeckUseCaseService(
            DeckRepository deckRepository, FlashcardRepository flashcardRepository, Validator validator) {
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.validator = validator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deck> getDecksByUserId(Long userId) {
        return deckRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
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
        return deckRepository.save(deck);
    }

    @Override
    @Transactional
    public void deleteDeck(Long id) {
        flashcardRepository.findByDeckId(id).forEach(card -> flashcardRepository.deleteById(card.getId()));
        deckRepository.deleteById(id);
    }
}
