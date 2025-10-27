package org.apolenkov.application.service.card;

import java.util.Collections;
import java.util.Optional;

import org.apolenkov.application.domain.port.CardRepository;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.service.stats.PaginationCountCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import jakarta.validation.Validator;

/**
 * Tests for cache invalidation when cards are created, updated, or deleted.
 * Ensures PaginationCountCache is properly invalidated to prevent stale count data.
 */
@ExtendWith(MockitoExtension.class)
class CardCacheInvalidationTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private Validator validator;

    @Mock
    private PaginationCountCache paginationCountCache;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CardUseCaseService service;

    @BeforeEach
    void setUp() {
        service = new CardUseCaseService(cardRepository, validator, paginationCountCache, eventPublisher);
    }

    @Test
    @DisplayName("Should invalidate cache when creating new card")
    void shouldInvalidateCacheOnCreate() {
        // Given
        Card newCard = new Card();
        newCard.setId(null); // New card
        newCard.setDeckId(5L);
        newCard.setFrontText("Hello");
        newCard.setBackText("Greetings");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When
        service.saveCard(newCard);

        // Then
        verify(cardRepository, times(1)).save(newCard);
        verify(paginationCountCache, times(1)).invalidate(5L);
    }

    @Test
    @DisplayName("Should invalidate cache when updating existing card")
    void shouldInvalidateCacheOnUpdate() {
        // Given
        Card existingCard = new Card();
        existingCard.setId(10L); // Existing card
        existingCard.setDeckId(7L);
        existingCard.setFrontText("Updated");
        existingCard.setBackText("Modified");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When
        service.saveCard(existingCard);

        // Then
        verify(cardRepository, times(1)).save(existingCard);
        verify(paginationCountCache, times(1)).invalidate(7L);
    }

    @Test
    @DisplayName("Should invalidate cache when deleting card")
    void shouldInvalidateCacheOnDelete() {
        // Given
        long cardId = 15L;
        Card card = new Card();
        card.setId(cardId);
        card.setDeckId(3L);
        card.setFrontText("To be deleted");
        card.setBackText("Will be removed");

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        // When
        service.deleteCard(cardId);

        // Then
        verify(cardRepository, times(1)).deleteById(cardId);
        verify(paginationCountCache, times(1)).invalidate(3L);
    }

    @Test
    @DisplayName("Should not invalidate cache when card not found on delete")
    void shouldNotInvalidateCacheWhenCardNotFound() {
        // Given
        long cardId = 99L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // When
        service.deleteCard(cardId);

        // Then
        verify(cardRepository, times(1)).deleteById(cardId);
        verify(paginationCountCache, times(0)).invalidate(any()); // NOT invalidated
    }

    @Test
    @DisplayName("Should invalidate cache for correct deck ID")
    void shouldInvalidateCacheForCorrectDeck() {
        // Given - multiple decks
        Card deck1Card = new Card();
        deck1Card.setId(null);
        deck1Card.setDeckId(1L);
        deck1Card.setFrontText("Deck 1");
        deck1Card.setBackText("First Deck");

        Card deck2Card = new Card();
        deck2Card.setId(null);
        deck2Card.setDeckId(2L);
        deck2Card.setFrontText("Deck 2");
        deck2Card.setBackText("Second Deck");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When
        service.saveCard(deck1Card);
        service.saveCard(deck2Card);

        // Then - each deck invalidated separately
        verify(paginationCountCache, times(1)).invalidate(1L);
        verify(paginationCountCache, times(1)).invalidate(2L);
    }

    @Test
    @DisplayName("Should invalidate cache multiple times for multiple operations")
    void shouldInvalidateCacheMultipleTimes() {
        // Given
        Card card = new Card();
        card.setId(null);
        card.setDeckId(10L);
        card.setFrontText("Test");
        card.setBackText("Testing");

        when(validator.validate(any())).thenReturn(Collections.emptySet());

        // When - add 3 cards to same deck
        service.saveCard(card);
        service.saveCard(card);
        service.saveCard(card);

        // Then - cache invalidated 3 times
        verify(paginationCountCache, times(3)).invalidate(10L);
    }
}
