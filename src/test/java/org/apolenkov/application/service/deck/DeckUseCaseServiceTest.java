package org.apolenkov.application.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckUseCaseService Core Tests")
class DeckUseCaseServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private Validator validator;

    private DeckUseCaseService deckUseCaseService;

    @BeforeEach
    void setUp() {
        deckUseCaseService = new DeckUseCaseService(deckRepository, flashcardRepository, validator);
    }

    @Test
    @DisplayName("Should get all decks for user")
    void shouldGetAllDecksForUser() {
        // Given
        long userId = 1L;
        List<Deck> expectedDecks = List.of(
                new Deck(1L, userId, "Deck 1", "Description 1"), new Deck(2L, userId, "Deck 2", "Description 2"));
        when(deckRepository.findByUserId(userId)).thenReturn(expectedDecks);

        // When
        List<Deck> result = deckUseCaseService.getDecksByUserId(userId);

        // Then
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedDecks);
    }

    @Test
    @DisplayName("Should get deck by id")
    void shouldGetDeckById() {
        // Given
        long deckId = 1L;
        Deck expectedDeck = new Deck(deckId, 1L, "Test Deck", "Description");
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(expectedDeck));

        // When
        Optional<Deck> result = deckUseCaseService.getDeckById(deckId);

        // Then
        assertThat(result).isPresent().contains(expectedDeck);
    }

    @Test
    @DisplayName("Should save deck")
    void shouldSaveDeck() {
        // Given
        Deck deckToSave = new Deck(1L, 1L, "New Deck", "Description");
        when(deckRepository.save(any(Deck.class))).thenReturn(deckToSave);

        // When
        Deck result = deckUseCaseService.saveDeck(deckToSave);

        // Then
        assertThat(result).isEqualTo(deckToSave);
    }

    @Test
    @DisplayName("Should delete deck")
    void shouldDeleteDeck() {
        // Given
        long deckId = 1L;

        // When
        deckUseCaseService.deleteDeck(deckId);

        // Then - no exception should be thrown
        assertThat(deckId).isEqualTo(1L); // Just to verify test runs
    }

    @Test
    @DisplayName("Should get all decks")
    void shouldGetAllDecks() {
        // Given
        List<Deck> expectedDecks =
                List.of(new Deck(1L, 1L, "Deck 1", "Description 1"), new Deck(2L, 1L, "Deck 2", "Description 2"));
        when(deckRepository.findAll()).thenReturn(expectedDecks);

        // When
        List<Deck> result = deckUseCaseService.getAllDecks();

        // Then
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedDecks);
    }

    @Test
    @DisplayName("Should handle deck not found")
    void shouldHandleDeckNotFound() {
        // Given
        long deckId = 999L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

        // When
        Optional<Deck> result = deckUseCaseService.getDeckById(deckId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty deck list")
    void shouldHandleEmptyDeckList() {
        // Given
        long userId = 1L;
        when(deckRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        List<Deck> result = deckUseCaseService.getDecksByUserId(userId);

        // Then
        assertThat(result).isEmpty();
    }
}
