package org.apolenkov.application.service.card;

import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for flashcard use cases and business operations.
 */
@Service
public class FlashcardUseCaseService implements FlashcardUseCase {

    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    /**
     * Creates a new FlashcardUseCaseService with required dependencies.
     *
     * @param flashcardRepositoryValue the repository for flashcard persistence operations
     * @param validatorValue the validator for flashcard data validation
     */
    public FlashcardUseCaseService(final FlashcardRepository flashcardRepositoryValue, final Validator validatorValue) {
        this.flashcardRepository = flashcardRepositoryValue;
        this.validator = validatorValue;
    }

    /**
     * Returns flashcards belonging to specific deck.
     *
     * @param deckId the ID of the deck to retrieve flashcards for
     * @return a list of flashcards belonging to the specified deck
     */
    @Override
    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsByDeckId(final long deckId) {
        return flashcardRepository.findByDeckId(deckId);
    }

    /**
     * Returns flashcard by ID.
     *
     * @param id the unique identifier of the flashcard to retrieve
     * @return an Optional containing the flashcard if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Flashcard> getFlashcardById(final long id) {
        return flashcardRepository.findById(id);
    }

    /**
     * Saves flashcard with validation.
     *
     * @param flashcard the flashcard to save
     * @return the saved flashcard
     * @throws IllegalArgumentException if flashcard validation fails
     */
    @Override
    @Transactional
    public Flashcard saveFlashcard(final Flashcard flashcard) {
        var violations = validator.validate(flashcard);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Validation failed: " + message);
        }
        return flashcardRepository.save(flashcard);
    }

    /**
     * Deletes flashcard by ID.
     *
     * @param id the unique identifier of the flashcard to delete
     */
    @Override
    @Transactional
    public void deleteFlashcard(final long id) {
        flashcardRepository.deleteById(id);
    }

    /**
     * Returns flashcards for practice sessions.
     *
     * @param deckId the ID of the deck to retrieve flashcards from
     * @param count the maximum number of flashcards to return
     * @param random whether to randomize the order of flashcards
     * @return a list of flashcards suitable for practice sessions
     */
    @Override
    @Transactional(readOnly = true)
    public List<Flashcard> getFlashcardsForPractice(final long deckId, final int count, final boolean random) {
        List<Flashcard> allCards = new ArrayList<>(flashcardRepository.findByDeckId(deckId));
        if (random) {
            Collections.shuffle(allCards);
        }
        return allCards.stream().limit(count).toList();
    }

    /**
     * Returns total number of flashcards in deck.
     *
     * @param deckId the ID of the deck to count flashcards for
     * @return the total number of flashcards in the specified deck
     */
    @Override
    @Transactional(readOnly = true)
    public long countByDeckId(final long deckId) {
        return flashcardRepository.countByDeckId(deckId);
    }
}
