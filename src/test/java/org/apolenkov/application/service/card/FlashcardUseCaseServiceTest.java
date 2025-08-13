package org.apolenkov.application.service.card;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apolenkov.application.infrastructure.repository.memory.InMemoryDeckRepository;
import org.apolenkov.application.infrastructure.repository.memory.InMemoryFlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardUseCaseServiceTest {

    private InMemoryDeckRepository deckRepo;
    private InMemoryFlashcardRepository cardRepo;
    private Validator validator;
    private FlashcardUseCaseService service;

    @BeforeEach
    void setUp() {
        deckRepo = new InMemoryDeckRepository();
        cardRepo = new InMemoryFlashcardRepository();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new FlashcardUseCaseService(deckRepo, cardRepo, validator);
    }

    @Test
    void saveFlashcard_valid_persists() {
        Deck deck = new Deck(null, 1L, "D", "");
        deckRepo.save(deck);
        Flashcard c = new Flashcard(null, deck.getId(), "F", "B");
        Flashcard saved = service.saveFlashcard(c);
        assertNotNull(saved.getId());
        List<Flashcard> list = service.getFlashcardsByDeckId(deck.getId());
        assertEquals(1, list.size());
    }

    @Test
    void saveFlashcard_invalid_throws() {
        Flashcard c = new Flashcard(null, null, "", "");
        assertThrows(IllegalArgumentException.class, () -> service.saveFlashcard(c));
    }
}


