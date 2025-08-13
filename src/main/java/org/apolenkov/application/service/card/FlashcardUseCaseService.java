package org.apolenkov.application.service.card;

import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlashcardUseCaseService implements FlashcardUseCase {

    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    public FlashcardUseCaseService(FlashcardRepository flashcardRepository, Validator validator) {
        this.flashcardRepository = flashcardRepository;
        this.validator = validator;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsByDeckId(Long deckId) {
        return flashcardRepository.findByDeckId(deckId);
    }

    @Override
    @Transactional(readOnly = true)
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
        return flashcardRepository.save(flashcard);
    }

    @Override
    @Transactional
    public void deleteFlashcard(Long id) {
        flashcardRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random) {
        List<Flashcard> allCards = new ArrayList<>(getFlashcardsByDeckId(deckId));
        if (random) {
            java.util.Collections.shuffle(allCards);
        }
        return allCards.stream().limit(count).collect(Collectors.toList());
    }
}
