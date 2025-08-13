package org.apolenkov.application.service.deck;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apolenkov.application.infrastructure.repository.memory.InMemoryDeckRepository;
import org.apolenkov.application.infrastructure.repository.memory.InMemoryFlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeckUseCaseServiceTest {

    private InMemoryDeckRepository deckRepo;
    private InMemoryFlashcardRepository cardRepo;
    private Validator validator;
    private DeckUseCaseService service;

    @BeforeEach
    void setUp() {
        deckRepo = new InMemoryDeckRepository();
        cardRepo = new InMemoryFlashcardRepository();
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        service = new DeckUseCaseService(deckRepo, cardRepo, validator);
    }

    @Test
    void saveDeck_valid_persists() {
        Deck d = new Deck(null, 1L, "Test", "Desc");
        Deck saved = service.saveDeck(d);
        assertNotNull(saved.getId());
        assertEquals(1, service.getAllDecks().size());
    }

    @Test
    void saveDeck_invalid_throws() {
        Deck d = new Deck(null, 1L, "", "Desc");
        assertThrows(IllegalArgumentException.class, () -> service.saveDeck(d));
    }

    @Test
    void deleteDeck_removesCards() {
        Deck d = service.saveDeck(new Deck(null, 1L, "D", ""));
        Flashcard c = new Flashcard(null, d.getId(), "F", "B");
        cardRepo.save(c);
        assertFalse(cardRepo.findByDeckId(d.getId()).isEmpty());

        service.deleteDeck(d.getId());
        assertTrue(deckRepo.findAll().isEmpty());
        assertTrue(cardRepo.findByDeckId(d.getId()).isEmpty());
    }
}
