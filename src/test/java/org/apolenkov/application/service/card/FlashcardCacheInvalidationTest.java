package org.apolenkov.application.service.card;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Optional;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.stats.PaginationCountCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Tests for cache invalidation when flashcards are created, updated, or deleted.
 * Ensures PaginationCountCache is properly invalidated to prevent stale count data.
 */
@ExtendWith(MockitoExtension.class)
class FlashcardCacheInvalidationTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private Validator validator;

    @Mock
    private PaginationCountCache paginationCountCache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private FlashcardUseCaseService service;

    @BeforeEach
    void setUp() {
        service = new FlashcardUseCaseService(flashcardRepository, validator, paginationCountCache, eventPublisher);
    }

    @Test
    @DisplayName("Should invalidate cache when creating new flashcard")
    void shouldInvalidateCacheOnCreate() {
        // Given
        Flashcard newFlashcard = new Flashcard();
        newFlashcard.setId(null); // New flashcard
        newFlashcard.setDeckId(5L);
        newFlashcard.setFrontText("Hello");
        newFlashcard.setBackText("Привет");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When
        service.saveFlashcard(newFlashcard);

        // Then
        verify(flashcardRepository, times(1)).save(newFlashcard);
        verify(paginationCountCache, times(1)).invalidate(5L);
    }

    @Test
    @DisplayName("Should invalidate cache when updating existing flashcard")
    void shouldInvalidateCacheOnUpdate() {
        // Given
        Flashcard existingFlashcard = new Flashcard();
        existingFlashcard.setId(10L); // Existing flashcard
        existingFlashcard.setDeckId(7L);
        existingFlashcard.setFrontText("Updated");
        existingFlashcard.setBackText("Обновлено");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When
        service.saveFlashcard(existingFlashcard);

        // Then
        verify(flashcardRepository, times(1)).save(existingFlashcard);
        verify(paginationCountCache, times(1)).invalidate(7L);
    }

    @Test
    @DisplayName("Should invalidate cache when deleting flashcard")
    void shouldInvalidateCacheOnDelete() {
        // Given
        long flashcardId = 15L;
        Flashcard flashcard = new Flashcard();
        flashcard.setId(flashcardId);
        flashcard.setDeckId(3L);
        flashcard.setFrontText("To be deleted");
        flashcard.setBackText("Будет удалено");

        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.of(flashcard));

        // When
        service.deleteFlashcard(flashcardId);

        // Then
        verify(flashcardRepository, times(1)).deleteById(flashcardId);
        verify(paginationCountCache, times(1)).invalidate(3L);
    }

    @Test
    @DisplayName("Should not invalidate cache when flashcard not found on delete")
    void shouldNotInvalidateCacheWhenFlashcardNotFound() {
        // Given
        long flashcardId = 99L;
        when(flashcardRepository.findById(flashcardId)).thenReturn(Optional.empty());

        // When
        service.deleteFlashcard(flashcardId);

        // Then
        verify(flashcardRepository, times(1)).deleteById(flashcardId);
        verify(paginationCountCache, times(0)).invalidate(any()); // NOT invalidated
    }

    @Test
    @DisplayName("Should invalidate cache for correct deck ID")
    void shouldInvalidateCacheForCorrectDeck() {
        // Given - multiple decks
        Flashcard deck1Card = new Flashcard();
        deck1Card.setId(null);
        deck1Card.setDeckId(1L);
        deck1Card.setFrontText("Deck 1");
        deck1Card.setBackText("Колода 1");

        Flashcard deck2Card = new Flashcard();
        deck2Card.setId(null);
        deck2Card.setDeckId(2L);
        deck2Card.setFrontText("Deck 2");
        deck2Card.setBackText("Колода 2");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When
        service.saveFlashcard(deck1Card);
        service.saveFlashcard(deck2Card);

        // Then - each deck invalidated separately
        verify(paginationCountCache, times(1)).invalidate(1L);
        verify(paginationCountCache, times(1)).invalidate(2L);
    }

    @Test
    @DisplayName("Should invalidate cache multiple times for multiple operations")
    void shouldInvalidateCacheMultipleTimes() {
        // Given
        Flashcard flashcard = new Flashcard();
        flashcard.setId(null);
        flashcard.setDeckId(10L);
        flashcard.setFrontText("Test");
        flashcard.setBackText("Тест");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When - add 3 cards to same deck
        service.saveFlashcard(flashcard);
        service.saveFlashcard(flashcard);
        service.saveFlashcard(flashcard);

        // Then - cache invalidated 3 times
        verify(paginationCountCache, times(3)).invalidate(10L);
    }
}
