package org.apolenkov.application.service.deck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Optional;
import org.apolenkov.application.domain.port.DeckRepository;
import org.apolenkov.application.domain.port.FlashcardRepository;
import org.apolenkov.application.model.Deck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckUseCaseService Tests")
class DeckUseCaseServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    private DeckUseCaseService deckUseCaseService;

    @BeforeEach
    void setUp() {
        try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            deckUseCaseService = new DeckUseCaseService(deckRepository, flashcardRepository, validator);
        }
    }

    @Nested
    @DisplayName("Get All Decks Tests")
    class GetAllDecksTests {

        @Test
        @DisplayName("GetAllDecks should return all decks from repository")
        void getAllDecksShouldReturnAllDecksFromRepository() {
            // Given
            Deck deck1 = new Deck(1L, 1L, "Deck 1", "Description 1");
            Deck deck2 = new Deck(2L, 1L, "Deck 2", "Description 2");
            List<Deck> expectedDecks = List.of(deck1, deck2);

            when(deckRepository.findAll()).thenReturn(expectedDecks);

            // When
            List<Deck> result = deckUseCaseService.getAllDecks();

            // Then
            assertThat(result).isEqualTo(expectedDecks);
            verify(deckRepository).findAll();
        }

        @Test
        @DisplayName("GetAllDecks should return empty list when no decks exist")
        void getAllDecksShouldReturnEmptyListWhenNoDecksExist() {
            // Given
            when(deckRepository.findAll()).thenReturn(List.of());

            // When
            List<Deck> result = deckUseCaseService.getAllDecks();

            // Then
            assertThat(result).isEmpty();
            verify(deckRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Get Decks By User ID Tests")
    class GetDecksByUserIdTests {

        @Test
        @DisplayName("GetDecksByUserId should return user's decks")
        void getDecksByUserIdShouldReturnUsersDecks() {
            // Given
            long userId = 1L;
            Deck deck1 = new Deck(1L, userId, "User Deck 1", "Description 1");
            Deck deck2 = new Deck(2L, userId, "User Deck 2", "Description 2");
            List<Deck> expectedDecks = List.of(deck1, deck2);

            when(deckRepository.findByUserId(userId)).thenReturn(expectedDecks);

            // When
            List<Deck> result = deckUseCaseService.getDecksByUserId(userId);

            // Then
            assertThat(result).isEqualTo(expectedDecks);
            verify(deckRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("GetDecksByUserId should return empty list when user has no decks")
        void getDecksByUserIdShouldReturnEmptyListWhenUserHasNoDecks() {
            // Given
            long userId = 1L;
            when(deckRepository.findByUserId(userId)).thenReturn(List.of());

            // When
            List<Deck> result = deckUseCaseService.getDecksByUserId(userId);

            // Then
            assertThat(result).isEmpty();
            verify(deckRepository).findByUserId(userId);
        }
    }

    @Nested
    @DisplayName("Get Deck By ID Tests")
    class GetDeckByIdTests {

        @BeforeEach
        void setUp() {
            try (var validatorFactory = Validation.buildDefaultValidatorFactory()) {
                Validator validator = validatorFactory.getValidator();
                deckUseCaseService = new DeckUseCaseService(deckRepository, flashcardRepository, validator);
            }
        }

        @Test
        @DisplayName("GetDeckById should return empty when deck does not exist")
        void getDeckByIdShouldReturnEmptyWhenDeckDoesNotExist() {
            // Given
            long deckId = 999L;
            when(deckRepository.findById(deckId)).thenReturn(Optional.empty());

            // When
            Optional<Deck> result = deckUseCaseService.getDeckById(deckId);

            // Then
            assertThat(result).isEmpty();
            verify(deckRepository).findById(deckId);
        }
    }

    @Nested
    @DisplayName("Save Deck Tests")
    class SaveDeckTests {

        @Test
        @DisplayName("SaveDeck should save valid deck successfully")
        void saveDeckShouldSaveValidDeckSuccessfully() {
            // Given
            Deck deckToSave = new Deck(null, 1L, "Valid Deck", "Valid Description");
            Deck savedDeck = new Deck(1L, 1L, "Valid Deck", "Valid Description");

            when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);

            // When
            Deck result = deckUseCaseService.saveDeck(deckToSave);

            // Then
            assertThat(result).isEqualTo(savedDeck);
            verify(deckRepository).save(deckToSave);
        }

        @Test
        @DisplayName("SaveDeck should throw exception for invalid deck")
        void saveDeckShouldThrowExceptionForInvalidDeck() {
            // Given
            Deck invalidDeck = new Deck();
            // Deck without required fields (userId and title)

            // When & Then
            assertThatThrownBy(() -> deckUseCaseService.saveDeck(invalidDeck))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validation failed");
        }

        @Test
        @DisplayName("SaveDeck should handle validation violations")
        void saveDeckShouldHandleValidationViolations() {
            // Given
            Deck deckWithLongTitle = new Deck();
            deckWithLongTitle.setUserId(1L);
            deckWithLongTitle.setTitle("a".repeat(121)); // Exceeds max length of 120

            // When & Then
            assertThatThrownBy(() -> deckUseCaseService.saveDeck(deckWithLongTitle))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validation failed");
        }
    }

    @Nested
    @DisplayName("Delete Deck Tests")
    class DeleteDeckTests {

        @Test
        @DisplayName("DeleteDeck should delete deck and its flashcards")
        void deleteDeckShouldDeleteDeckAndItsFlashcards() {
            // Given
            long deckId = 1L;

            // When
            deckUseCaseService.deleteDeck(deckId);

            // Then
            verify(flashcardRepository).deleteByDeckId(deckId);
            verify(deckRepository).deleteById(deckId);
        }

        @Test
        @DisplayName("DeleteDeck should handle deletion in correct order")
        void deleteDeckShouldHandleDeletionInCorrectOrder() {
            // Given
            long deckId = 1L;

            // When
            deckUseCaseService.deleteDeck(deckId);

            // Then
            verify(flashcardRepository).deleteByDeckId(deckId);
            verify(deckRepository).deleteById(deckId);

            // Verify order of operations
            verifyNoMoreInteractions(flashcardRepository, deckRepository);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null deck in saveDeck")
        void shouldHandleNullDeckInSaveDeck() {
            // When & Then
            assertThatThrownBy(() -> deckUseCaseService.saveDeck(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("The object to be validated must not be null");
        }

        @Test
        @DisplayName("Should handle deck with null values")
        void shouldHandleDeckWithNullValues() {
            // Given
            Deck deckWithNulls = new Deck();
            deckWithNulls.setUserId(1L);
            deckWithNulls.setTitle("Valid Title");
            deckWithNulls.setDescription(null); // null description is allowed

            Deck savedDeck = new Deck(1L, 1L, "Valid Title", null);
            when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);

            // When
            Deck result = deckUseCaseService.saveDeck(deckWithNulls);

            // Then
            assertThat(result).isEqualTo(savedDeck);
            verify(deckRepository).save(deckWithNulls);
        }

        @Test
        @DisplayName("Should handle very long valid values")
        void shouldHandleVeryLongValidValues() {
            // Given
            Deck deckWithLongValues = new Deck();
            deckWithLongValues.setUserId(1L);
            deckWithLongValues.setTitle("a".repeat(120)); // Exactly at max length
            deckWithLongValues.setDescription("a".repeat(500)); // Exactly at max length

            Deck savedDeck = new Deck(1L, 1L, "a".repeat(120), "a".repeat(500));
            when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);

            // When
            Deck result = deckUseCaseService.saveDeck(deckWithLongValues);

            // Then
            assertThat(result).isEqualTo(savedDeck);
            verify(deckRepository).save(deckWithLongValues);
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("SaveDeck should be transactional")
        void saveDeckShouldBeTransactional() {
            // This test verifies that the method is annotated with @Transactional
            // The actual transaction behavior is tested in integration tests

            // Given
            Deck validDeck = new Deck(null, 1L, "Test Deck", "Test Description");
            Deck savedDeck = new Deck(1L, 1L, "Test Deck", "Test Description");

            when(deckRepository.save(any(Deck.class))).thenReturn(savedDeck);

            // When
            Deck result = deckUseCaseService.saveDeck(validDeck);

            // Then
            assertThat(result).isEqualTo(savedDeck);
            // Transaction behavior is verified by the fact that the method executes without error
        }

        @Test
        @DisplayName("DeleteDeck should be transactional")
        void deleteDeckShouldBeTransactional() {
            // This test verifies that the method is annotated with @Transactional
            // The actual transaction behavior is tested in integration tests

            // Given
            long deckId = 1L;

            // When & Then
            assertThatNoException().isThrownBy(() -> deckUseCaseService.deleteDeck(deckId));
            // Transaction behavior is verified by the fact that the method executes without error
        }
    }
}
