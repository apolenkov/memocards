package org.apolenkov.application.service.card;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.apolenkov.application.infrastructure.repository.memory.InMemoryFlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlashcardUseCaseServiceTest {

  private InMemoryFlashcardRepository cardRepo;
  private Validator validator;
  private FlashcardUseCaseService service;

  @BeforeEach
  void setUp() {
    cardRepo = new InMemoryFlashcardRepository();
    validator = Validation.buildDefaultValidatorFactory().getValidator();
    service = new FlashcardUseCaseService(cardRepo, validator);
  }

  @Test
  void saveFlashcard_valid_persists() {
    Deck deck = new Deck(null, 1L, "D", "");
    deck.setId(1L);
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
