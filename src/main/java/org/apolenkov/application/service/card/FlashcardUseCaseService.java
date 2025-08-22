package org.apolenkov.application.service.card;

import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apolenkov.application.config.TransactionAnnotations;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.springframework.stereotype.Service;

/**
 * Service implementation for flashcard use cases and business operations.
 *
 * <p>This service implements the FlashcardUseCase interface and provides
 * comprehensive flashcard management functionality including CRUD operations,
 * validation, and practice session preparation. It handles both individual
 * flashcard operations and bulk operations for practice sessions.</p>
 *
 * <p>The service includes validation logic to ensure data integrity and
 * provides methods for retrieving flashcards in various configurations
 * suitable for different use cases.</p>
 *
 */
@Service
public class FlashcardUseCaseService implements FlashcardUseCase {

    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    /**
     * Constructs a new FlashcardUseCaseService with required dependencies.
     *
     * @param flashcardRepository the repository for flashcard persistence operations
     * @param validator the validator for flashcard data validation
     */
    public FlashcardUseCaseService(FlashcardRepository flashcardRepository, Validator validator) {
        this.flashcardRepository = flashcardRepository;
        this.validator = validator;
    }

    /**
     * Retrieves all flashcards belonging to a specific deck.
     *
     * <p>Returns a list of all flashcards associated with the specified deck ID.
     * This method is typically used for deck management and display purposes.</p>
     *
     * @param deckId the ID of the deck to retrieve flashcards for
     * @return a list of flashcards belonging to the specified deck
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Flashcard> getFlashcardsByDeckId(Long deckId) {
        return flashcardRepository.findByDeckId(deckId);
    }

    /**
     * Retrieves a specific flashcard by its unique identifier.
     *
     * <p>Returns an Optional containing the flashcard if found, or an empty
     * Optional if no flashcard exists with the specified ID.</p>
     *
     * @param id the unique identifier of the flashcard to retrieve
     * @return an Optional containing the flashcard if found, empty otherwise
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public Optional<Flashcard> getFlashcardById(Long id) {
        return flashcardRepository.findById(id);
    }

    /**
     * Saves a flashcard to the repository.
     *
     * <p>Validates the flashcard data before saving to ensure data integrity.
     * If validation fails, an IllegalArgumentException is thrown with details
     * about the validation errors.</p>
     *
     * @param flashcard the flashcard to save
     * @return the saved flashcard (may have generated ID if new)
     * @throws IllegalArgumentException if flashcard validation fails
     */
    @Override
    @TransactionAnnotations.WriteTransaction
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

    /**
     * Deletes a flashcard by its unique identifier.
     *
     * <p>Removes the specified flashcard from the repository. If no flashcard
     * exists with the specified ID, the operation completes without error.</p>
     *
     * @param id the unique identifier of the flashcard to delete
     */
    @Override
    @TransactionAnnotations.DeleteTransaction
    public void deleteFlashcard(Long id) {
        flashcardRepository.deleteById(id);
    }

    /**
     * Retrieves flashcards for practice sessions.
     *
     * <p>Returns a subset of flashcards from the specified deck, optionally
     * randomized and limited to the specified count. This method is designed
     * for preparing practice sessions with configurable parameters.</p>
     *
     * @param deckId the ID of the deck to retrieve flashcards from
     * @param count the maximum number of flashcards to return
     * @param random whether to randomize the order of flashcards
     * @return a list of flashcards suitable for practice sessions
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random) {
        List<Flashcard> allCards = new ArrayList<>(getFlashcardsByDeckId(deckId));
        if (random) {
            java.util.Collections.shuffle(allCards);
        }
        return allCards.stream().limit(count).collect(Collectors.toList());
    }

    /**
     * Counts the total number of flashcards in a specific deck.
     *
     * <p>Returns the total count of flashcards associated with the specified
     * deck ID. This method is useful for deck statistics and validation.</p>
     *
     * @param deckId the ID of the deck to count flashcards for
     * @return the total number of flashcards in the specified deck
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public long countByDeckId(Long deckId) {
        return flashcardRepository.countByDeckId(deckId);
    }
}
