package org.apolenkov.application.service.deck;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apolenkov.application.config.TransactionAnnotations;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.usecase.DeckUseCase;
import org.springframework.stereotype.Service;

/**
 * Service implementation for deck-related business operations.
 */
@Service
public class DeckUseCaseService implements DeckUseCase {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    /**
     * Creates service with required dependencies.
     *
     * @param deckRepositoryValue the repository for deck operations
     * @param flashcardRepositoryValue the repository for flashcard operations
     * @param validatorValue the validator for input validation
     * @throws IllegalArgumentException if any parameter is null
     */
    public DeckUseCaseService(
            final DeckRepository deckRepositoryValue,
            final FlashcardRepository flashcardRepositoryValue,
            final Validator validatorValue) {
        if (deckRepositoryValue == null) {
            throw new IllegalArgumentException("DeckRepository cannot be null");
        }
        if (flashcardRepositoryValue == null) {
            throw new IllegalArgumentException("FlashcardRepository cannot be null");
        }
        if (validatorValue == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }

        this.deckRepository = deckRepositoryValue;
        this.flashcardRepository = flashcardRepositoryValue;
        this.validator = validatorValue;
    }

    /**
     * Returns all decks in the system.
     *
     * @return a list of all decks in the system, or empty list if none exist
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    /**
     * Returns decks owned by specific user.
     *
     * @param userId the ID of the user whose decks to retrieve
     * @return a list of decks owned by the specified user, or empty list if none exist
     * @throws IllegalArgumentException if userId is null
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Deck> getDecksByUserId(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return deckRepository.findByUserId(userId);
    }

    /**
     * Returns deck by ID.
     *
     * @param id the unique identifier of the deck to retrieve
     * @return an Optional containing the deck if found, or empty Optional if not found
     * @throws IllegalArgumentException if id is null
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public Optional<Deck> getDeckById(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        return deckRepository.findById(id);
    }

    /**
     * Saves deck to system with validation.
     *
     * @param deck the deck to save (create or update)
     * @return the saved deck with updated fields
     * @throws IllegalArgumentException if deck is null or validation fails
     * @throws RuntimeException if database operation fails
     */
    @Override
    @TransactionAnnotations.WriteTransaction
    public Deck saveDeck(final Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("The object to be validated must not be null");
        }

        var violations = validator.validate(deck);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Validation failed: " + message);
        }
        return deckRepository.save(deck);
    }

    /**
     * Deletes deck and all associated flashcards.
     *
     * @param id the unique identifier of the deck to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if database operation fails or deck doesn't exist
     */
    @Override
    @TransactionAnnotations.DeleteTransaction
    public void deleteDeck(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }

        // Delete associated flashcards first to maintain referential integrity
        flashcardRepository.deleteByDeckId(id);
        // Then delete the deck itself
        deckRepository.deleteById(id);
    }
}
