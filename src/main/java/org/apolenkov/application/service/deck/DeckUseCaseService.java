package org.apolenkov.application.service.deck;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.config.TransactionAnnotations;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.springframework.stereotype.Service;

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
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Deck> getDecksByUserId(Long userId) {
        return deckRepository.findByUserId(userId);
    }

    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public Optional<Deck> getDeckById(Long id) {
        return deckRepository.findById(id);
    }

    @Override
    @TransactionAnnotations.WriteTransaction
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
    @TransactionAnnotations.DeleteTransaction
    public void deleteDeck(Long id) {
        flashcardRepository.deleteByDeckId(id);
        deckRepository.deleteById(id);
    }
}
