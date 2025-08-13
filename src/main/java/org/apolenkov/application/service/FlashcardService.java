package org.apolenkov.application.service;

import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.domain.port.UserRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service orchestrating operations on decks, cards and users.
 * Delegates persistence to repositories (ports).
 */
@Service
@Deprecated(since = "refactor-usecases", forRemoval = true)
public class FlashcardService implements 
        org.apolenkov.application.application.usecase.DeckUseCase,
        org.apolenkov.application.application.usecase.FlashcardUseCase,
        org.apolenkov.application.application.usecase.UserUseCase {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final Validator validator;

    public FlashcardService(UserRepository userRepository,
                            DeckRepository deckRepository,
                            FlashcardRepository flashcardRepository,
                            Validator validator) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.validator = validator;
    }

    // User methods
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User getCurrentUser() {
        // For demo: first user
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No users initialized"));
    }

    // Deck methods
    public List<Deck> getAllDecks() {
        return deckRepository.findAll();
    }

    public List<Deck> getDecksByUserId(Long userId) {
        return deckRepository.findByUserId(userId);
    }

    public Optional<Deck> getDeckById(Long id) {
        return deckRepository.findById(id);
    }

    @Transactional
    public Deck saveDeck(Deck deck) {
        validate(deck);
        Deck saved = deckRepository.save(deck);
        // keep deck.flashcards in sync
        List<Flashcard> deckCards = flashcardRepository.findByDeckId(saved.getId());
        saved.setFlashcards(deckCards);
        return saved;
    }

    @Transactional
    public void deleteDeck(Long id) {
        // delete cards of deck
        flashcardRepository.findByDeckId(id).forEach(card -> flashcardRepository.deleteById(card.getId()));
        deckRepository.deleteById(id);
    }

    // Flashcard methods
    public List<Flashcard> getFlashcardsByDeckId(Long deckId) {
        return flashcardRepository.findByDeckId(deckId);
    }

    public Optional<Flashcard> getFlashcardById(Long id) {
        return flashcardRepository.findById(id);
    }

    @Transactional
    public Flashcard saveFlashcard(Flashcard flashcard) {
        validate(flashcard);
        Flashcard saved = flashcardRepository.save(flashcard);
        // Update deck's flashcards list
        getDeckById(saved.getDeckId()).ifPresent(deck -> {
            List<Flashcard> deckCards = getFlashcardsByDeckId(deck.getId());
            deck.setFlashcards(deckCards);
            deckRepository.save(deck);
        });
        return saved;
    }

    private <T> void validate(T bean) {
        var violations = validator.validate(bean);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new IllegalArgumentException("Validation failed: " + message);
        }
    }

    @Transactional
    public void deleteFlashcard(Long id) {
        Optional<Flashcard> flashcard = getFlashcardById(id);
        if (flashcard.isPresent()) {
            Long deckId = flashcard.get().getDeckId();
            flashcardRepository.deleteById(id);
            // Update deck's flashcards list
            getDeckById(deckId).ifPresent(deck -> {
                List<Flashcard> deckCards = getFlashcardsByDeckId(deck.getId());
                deck.setFlashcards(deckCards);
                deckRepository.save(deck);
            });
        }
    }

    // Practice methods
    public List<Flashcard> getFlashcardsForPractice(Long deckId, int count, boolean random) {
        List<Flashcard> allCards = new ArrayList<>(getFlashcardsByDeckId(deckId));
        if (random) {
            java.util.Collections.shuffle(allCards);
        }
        return allCards.stream().limit(count).collect(Collectors.toList());
    }
}
