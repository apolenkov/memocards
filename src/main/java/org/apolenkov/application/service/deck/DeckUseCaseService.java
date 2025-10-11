package org.apolenkov.application.service.deck;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for deck-related business operations.
 */
@Service
public class DeckUseCaseService implements DeckUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeckUseCaseService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

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
    @Transactional(readOnly = true)
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    /**
     * Returns decks owned by specific user.
     *
     * @param userId the ID of the user whose decks to retrieve
     * @return a list of decks owned by the specified user, or empty list if none exist
     * @throws IllegalArgumentException if userId is invalid
     */
    @Override
    @Transactional(readOnly = true)
    public List<Deck> getDecksByUserId(final long userId) {
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
    @Transactional(readOnly = true)
    public Optional<Deck> getDeckById(final long id) {
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
    @Transactional
    public Deck saveDeck(final Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("The object to be validated must not be null");
        }

        LOGGER.debug("Saving deck: title='{}', userId={}", deck.getTitle(), deck.getUserId());

        var violations = validator.validate(deck);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + CoreConstants.SEPARATOR_SPACE + v.getMessage())
                    .collect(Collectors.joining(", "));
            LOGGER.warn("Deck validation failed: {}", message);
            throw new IllegalArgumentException("Validation failed: " + message);
        }

        Deck savedDeck = deckRepository.save(deck);

        // Log business event
        LOGGER.info(
                "Deck saved: id={}, title='{}', userId={}, isNew={}",
                savedDeck.getId(),
                savedDeck.getTitle(),
                savedDeck.getUserId(),
                deck.getId() == null);

        // Audit log
        AUDIT_LOGGER.info(
                "Deck {}: title='{}', userId={}, isNew={}",
                savedDeck.getId(),
                savedDeck.getTitle(),
                savedDeck.getUserId(),
                deck.getId() == null);

        return savedDeck;
    }

    /**
     * Deletes deck and all associated flashcards.
     *
     * @param id the unique identifier of the deck to delete
     * @throws IllegalArgumentException if id is invalid
     * @throws RuntimeException if database operation fails or deck doesn't exist
     */
    @Override
    @Transactional
    public void deleteDeck(final long id) {
        LOGGER.debug("Deleting deck with ID {}", id);

        // Get deck info before deletion for logging
        Optional<Deck> deckOpt = deckRepository.findById(id);
        if (deckOpt.isEmpty()) {
            LOGGER.warn("Attempted to delete non-existent deck {}", id);
            return;
        }

        Deck deck = deckOpt.get();
        LOGGER.info("Deleting deck '{}' (ID: {}) for user {}", deck.getTitle(), id, deck.getUserId());

        // Delete associated flashcards first to maintain referential integrity
        flashcardRepository.deleteByDeckId(id);
        LOGGER.debug("Deleted flashcards for deck {}", id);

        // Then delete the deck itself
        deckRepository.deleteById(id);

        // Log business event
        LOGGER.info("Deck deleted: id={}, title='{}', userId={}", id, deck.getTitle(), deck.getUserId());

        // Audit log
        AUDIT_LOGGER.warn("Deck deleted: id={}, title='{}', userId={}", id, deck.getTitle(), deck.getUserId());
    }
}
