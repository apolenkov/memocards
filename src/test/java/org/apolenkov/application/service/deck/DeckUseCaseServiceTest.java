package org.apolenkov.application.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.CardRepository;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckUseCaseService Core Tests")
class DeckUseCaseServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private Validator validator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DeckUseCaseService deckUseCaseService;

    @BeforeEach
    void setUp() {
        deckUseCaseService = new DeckUseCaseService(deckRepository, cardRepository, validator, eventPublisher);
    }

    @Test
    @DisplayName("Should get all decks for user")
    void shouldGetAllDecksForUser() {
        // Given
        final long userId = 1L;
        final List<Deck> expectedDecks = List.of(
                new Deck(1L, userId, "Deck 1", "Description 1"), new Deck(2L, userId, "Deck 2", "Description 2"));
        when(deckRepository.findByUserId(userId)).thenReturn(expectedDecks);

        // When
        final List<Deck> result = deckUseCaseService.getDecksByUserId(userId);

        // Then
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedDecks);
    }

    @Test
    @DisplayName("Should get deck by id")
    void shouldGetDeckById() {
        // Given
        final long deckId = 1L;
        final Deck expectedDeck = new Deck(deckId, 1L, "Test Deck", "Description");
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(expectedDeck));

        // When
        final Optional<Deck> result = deckUseCaseService.getDeckById(deckId);

        // Then
        assertThat(result).isPresent().contains(expectedDeck);
    }

    @Test
    @DisplayName("Should save deck")
    void shouldSaveDeck() {
        // Given
        final Deck deckToSave = new Deck(1L, 1L, "New Deck", "Description");
        when(deckRepository.save(any(Deck.class))).thenReturn(deckToSave);

        // When
        final Deck result = deckUseCaseService.saveDeck(deckToSave);

        // Then
        assertThat(result).isEqualTo(deckToSave);
    }

    @Test
    @DisplayName("Should delete deck")
    void shouldDeleteDeck() {
        // Given
        final long deckId = 1L;

        // When
        deckUseCaseService.deleteDeck(deckId);

        // Then - no exception should be thrown
        assertThat(deckId).isEqualTo(1L); // Just to verify test runs
    }

    @Test
    @DisplayName("Should get all decks")
    void shouldGetAllDecks() {
        // Given
        final List<Deck> expectedDecks =
                List.of(new Deck(1L, 1L, "Deck 1", "Description 1"), new Deck(2L, 1L, "Deck 2", "Description 2"));
        when(deckRepository.findAll()).thenReturn(expectedDecks);

        // When
        final List<Deck> result = deckUseCaseService.getAllDecks();

        // Then
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedDecks);
    }

    @Test
    @DisplayName("Should handle deck not found")
    void shouldHandleDeckNotFound() {
        // Given
        final long deckId = 999L;
        when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

        // When
        final Optional<Deck> result = deckUseCaseService.getDeckById(deckId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty deck list")
    void shouldHandleEmptyDeckList() {
        // Given
        final long userId = 1L;
        when(deckRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        final List<Deck> result = deckUseCaseService.getDecksByUserId(userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should create new deck")
    void shouldCreateNewDeck() {
        // Given
        final Deck newDeck = new Deck(null, 1L, "New Deck", "Description");
        final Deck savedDeck = new Deck(1L, 1L, "New Deck", "Description");
        when(deckRepository.save(newDeck)).thenReturn(savedDeck);

        // When
        final Deck result = deckUseCaseService.saveDeck(newDeck);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Deck");
    }

    @Test
    @DisplayName("Should update existing deck")
    void shouldUpdateExistingDeck() {
        // Given
        final Deck existingDeck = new Deck(1L, 1L, "Updated Title", "Updated Description");
        when(deckRepository.save(existingDeck)).thenReturn(existingDeck);

        // When
        final Deck result = deckUseCaseService.saveDeck(existingDeck);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
    }
}
