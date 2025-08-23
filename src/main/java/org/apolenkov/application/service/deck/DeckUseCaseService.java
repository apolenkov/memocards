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

/**
 * Service implementation for deck-related business operations.
 *
 * <p>This service implements the {@link DeckUseCase} interface and provides
 * the core business logic for managing flashcard decks. It handles:</p>
 * <ul>
 *   <li><strong>CRUD Operations:</strong> Creating, reading, updating, and deleting decks</li>
 *   <li><strong>Validation:</strong> Ensuring deck data integrity through Bean Validation</li>
 *   <li><strong>Transaction Management:</strong> Proper transaction boundaries for different operations</li>
 *   <li><strong>Data Consistency:</strong> Maintaining referential integrity between decks and flashcards</li>
 * </ul>
 *
 * <p>The service uses a layered architecture approach:</p>
 * <ul>
 *   <li><strong>Repository Layer:</strong> Data access through {@link DeckRepository} and {@link FlashcardRepository}</li>
 *   <li><strong>Validation Layer:</strong> Input validation using Jakarta Bean Validation</li>
 *   <li><strong>Transaction Layer:</strong> Transaction management through custom annotations</li>
 * </ul>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * // Get all decks for a user
 * List<Deck> userDecks = deckUseCaseService.getDecksByUserId(userId);
 *
 * // Create and save a new deck
 * Deck newDeck = Deck.create(userId, "My Deck", "Description");
 * Deck savedDeck = deckUseCaseService.saveDeck(newDeck);
 *
 * // Delete a deck (also removes associated flashcards)
 * deckUseCaseService.deleteDeck(deckId);
 * }</pre>
 *
 * @see DeckUseCase
 * @see DeckRepository
 * @see FlashcardRepository
 * @see Deck
 * @see TransactionAnnotations
 * @see jakarta.validation.Validator
 */
@Service
public class DeckUseCaseService implements DeckUseCase {

    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    /**
     * Constructs a new DeckUseCaseService with the required dependencies.
     *
     * <p>This constructor initializes the service with:</p>
     * <ul>
     *   <li><strong>DeckRepository:</strong> For deck data persistence operations</li>
     *   <li><strong>FlashcardRepository:</strong> For flashcard data operations</li>
     *   <li><strong>Validator:</strong> For input validation using Bean Validation</li>
     * </ul>
     *
     * @param deckRepository the repository for deck operations
     * @param flashcardRepository the repository for flashcard operations
     * @param validator the validator for input validation
     * @throws IllegalArgumentException if any parameter is null
     */
    public DeckUseCaseService(
            DeckRepository deckRepository, FlashcardRepository flashcardRepository, Validator validator) {
        if (deckRepository == null) {
            throw new IllegalArgumentException("DeckRepository cannot be null");
        }
        if (flashcardRepository == null) {
            throw new IllegalArgumentException("FlashcardRepository cannot be null");
        }
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }

        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.validator = validator;
    }

    /**
     * Retrieves all decks in the system.
     *
     * <p>This method performs a read-only operation to fetch all available decks.
     * It uses a read-only transaction for optimal performance when no data
     * modification is needed.</p>
     *
     * <p><strong>Note:</strong> This method may return a large number of results
     * and should be used with caution in production environments. Consider
     * implementing pagination for better performance.</p>
     *
     * @return a list of all decks in the system, or empty list if none exist
     * @see TransactionAnnotations.ReadOnlyTransaction
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    /**
     * Retrieves all decks owned by a specific user.
     *
     * <p>This method performs a read-only operation to fetch decks belonging
     * to the specified user. It uses a read-only transaction for optimal
     * performance.</p>
     *
     * <p>The method returns an empty list if the user has no decks or if
     * the user ID doesn't exist in the system.</p>
     *
     * @param userId the ID of the user whose decks to retrieve
     * @return a list of decks owned by the specified user, or empty list if none exist
     * @throws IllegalArgumentException if userId is null
     * @see TransactionAnnotations.ReadOnlyTransaction
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public List<Deck> getDecksByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return deckRepository.findByUserId(userId);
    }

    /**
     * Retrieves a specific deck by its unique identifier.
     *
     * <p>This method performs a read-only operation to fetch a single deck
     * by its ID. It uses a read-only transaction for optimal performance.</p>
     *
     * <p>The method returns an empty {@link Optional} if no deck exists
     * with the specified ID.</p>
     *
     * @param id the unique identifier of the deck to retrieve
     * @return an Optional containing the deck if found, or empty Optional if not found
     * @throws IllegalArgumentException if id is null
     * @see TransactionAnnotations.ReadOnlyTransaction
     * @see Optional
     */
    @Override
    @TransactionAnnotations.ReadOnlyTransaction
    public Optional<Deck> getDeckById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }
        return deckRepository.findById(id);
    }

    /**
     * Saves a deck to the system.
     *
     * <p>This method performs a write operation that may create a new deck
     * or update an existing one. It includes comprehensive validation and
     * uses a write transaction to ensure data consistency.</p>
     *
     * <p>The validation process checks:</p>
     * <ul>
     *   <li><strong>Bean Validation:</strong> All validation constraints defined on the Deck model</li>
     *   <li><strong>Business Rules:</strong> Custom business logic validation</li>
     *   <li><strong>Data Integrity:</strong> Referential integrity and data consistency</li>
     * </ul>
     *
     * <p>If validation fails, the method throws an {@link IllegalArgumentException}
     * with detailed information about the validation errors.</p>
     *
     * @param deck the deck to save (create or update)
     * @return the saved deck with updated fields (e.g., generated ID, timestamps)
     * @throws IllegalArgumentException if deck is null or validation fails
     * @throws RuntimeException if database operation fails
     * @see TransactionAnnotations.WriteTransaction
     * @see jakarta.validation.Validator#validate(Object)
     */
    @Override
    @TransactionAnnotations.WriteTransaction
    public Deck saveDeck(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("The object to be validated must not be null");
        }

        var violations = validator.validate(deck);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new IllegalArgumentException("Validation failed: " + message);
        }
        return deckRepository.save(deck);
    }

    /**
     * Deletes a deck and all its associated flashcards.
     *
     * <p>This method performs a delete operation that removes both the deck
     * and all flashcards that belong to it. It uses a delete transaction
     * to ensure data consistency and proper cleanup.</p>
     *
     * <p><strong>Important:</strong> This operation is irreversible and will
     * permanently remove all data associated with the deck, including:</p>
     * <ul>
     *   <li>The deck itself</li>
     *   <li>All flashcards in the deck</li>
     *   <li>Any associated statistics or progress data</li>
     * </ul>
     *
     * <p>The deletion is performed in the correct order to maintain referential
     * integrity: first flashcards, then the deck itself.</p>
     *
     * @param id the unique identifier of the deck to delete
     * @throws IllegalArgumentException if id is null
     * @throws RuntimeException if database operation fails or deck doesn't exist
     * @see TransactionAnnotations.DeleteTransaction
     */
    @Override
    @TransactionAnnotations.DeleteTransaction
    public void deleteDeck(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Deck ID cannot be null");
        }

        // Delete associated flashcards first to maintain referential integrity
        flashcardRepository.deleteByDeckId(id);
        // Then delete the deck itself
        deckRepository.deleteById(id);
    }
}
