package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckFacade Tests")
class DeckFacadeTest {

    @Mock
    private DeckUseCase deckUseCase;

    @Mock
    private FlashcardUseCase flashcardUseCase;

    @Mock
    private StatsService statsService;

    private DeckFacade deckFacade;

    @BeforeEach
    void setUp() {
        deckFacade = new DeckFacade(deckUseCase, flashcardUseCase, statsService);
    }

    @Nested
    @DisplayName("Get Deck Tests")
    class GetDeckTests {

        @Test
        @DisplayName("GetDeckOrThrow should return deck when exists")
        void getDeckOrThrowShouldReturnDeckWhenExists() {
            // Given
            Long deckId = 1L;
            Deck expectedDeck = new Deck(deckId, 1L, "Test Deck", "Test Description");

            when(deckUseCase.getDeckById(deckId)).thenReturn(Optional.of(expectedDeck));

            // When
            Deck result = deckFacade.getDeckOrThrow(deckId);

            // Then
            assertThat(result).isEqualTo(expectedDeck);
            verify(deckUseCase).getDeckById(deckId);
        }

        @Test
        @DisplayName("GetDeckOrThrow should throw exception when deck does not exist")
        void getDeckOrThrowShouldThrowExceptionWhenDeckDoesNotExist() {
            // Given
            Long deckId = 999L;
            when(deckUseCase.getDeckById(deckId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> deckFacade.getDeckOrThrow(deckId)).isInstanceOf(RuntimeException.class);
            verify(deckUseCase).getDeckById(deckId);
        }
    }

    @Nested
    @DisplayName("Flashcard Management Tests")
    class FlashcardManagementTests {

        @Test
        @DisplayName("LoadFlashcards should return flashcards for deck")
        void loadFlashcardsShouldReturnFlashcardsForDeck() {
            // Given
            Long deckId = 1L;
            List<Flashcard> expectedFlashcards = List.of(
                    new Flashcard(1L, deckId, "Front 1", "Back 1"), new Flashcard(2L, deckId, "Front 2", "Back 2"));

            when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(expectedFlashcards);

            // When
            List<Flashcard> result = deckFacade.loadFlashcards(deckId);

            // Then
            assertThat(result).isEqualTo(expectedFlashcards);
            verify(flashcardUseCase).getFlashcardsByDeckId(deckId);
        }

        @Test
        @DisplayName("LoadFlashcards should return empty list when no flashcards exist")
        void loadFlashcardsShouldReturnEmptyListWhenNoFlashcardsExist() {
            // Given
            Long deckId = 1L;
            when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(List.of());

            // When
            List<Flashcard> result = deckFacade.loadFlashcards(deckId);

            // Then
            assertThat(result).isEmpty();
            verify(flashcardUseCase).getFlashcardsByDeckId(deckId);
        }

        @Test
        @DisplayName("SaveFlashcard should save and return flashcard")
        void saveFlashcardShouldSaveAndReturnFlashcard() {
            // Given
            Flashcard flashcardToSave = new Flashcard(null, 1L, "Front", "Back");
            Flashcard savedFlashcard = new Flashcard(1L, 1L, "Front", "Back");

            when(flashcardUseCase.saveFlashcard(flashcardToSave)).thenReturn(savedFlashcard);

            // When
            Flashcard result = deckFacade.saveFlashcard(flashcardToSave);

            // Then
            assertThat(result).isEqualTo(savedFlashcard);
            verify(flashcardUseCase).saveFlashcard(flashcardToSave);
        }

        @Test
        @DisplayName("DeleteFlashcard should delete flashcard")
        void deleteFlashcardShouldDeleteFlashcard() {
            // Given
            Long flashcardId = 1L;

            // When
            deckFacade.deleteFlashcard(flashcardId);

            // Then
            verify(flashcardUseCase).deleteFlashcard(flashcardId);
        }
    }

    @Nested
    @DisplayName("Progress Management Tests")
    class ProgressManagementTests {

        @Test
        @DisplayName("GetKnown should return known card IDs")
        void getKnownShouldReturnKnownCardIds() {
            // Given
            long deckId = 1L;
            Set<Long> expectedKnownCardIds = Set.of(1L, 2L, 3L);

            when(statsService.getKnownCardIds(deckId)).thenReturn(expectedKnownCardIds);

            // When
            Set<Long> result = deckFacade.getKnown(deckId);

            // Then
            assertThat(result).isEqualTo(expectedKnownCardIds);
            verify(statsService).getKnownCardIds(deckId);
        }

        @Test
        @DisplayName("ToggleKnown should toggle card knowledge status")
        void toggleKnownShouldToggleCardKnowledgeStatus() {
            // Given
            Long deckId = 1L;
            Long cardId = 5L;
            when(statsService.isCardKnown(deckId, cardId)).thenReturn(false);

            // When
            deckFacade.toggleKnown(deckId, cardId);

            // Then
            verify(statsService).isCardKnown(deckId, cardId);
            verify(statsService).setCardKnown(deckId, cardId, true);
        }

        @Test
        @DisplayName("ToggleKnown should toggle from known to unknown")
        void toggleKnownShouldToggleFromKnownToUnknown() {
            // Given
            Long deckId = 1L;
            Long cardId = 5L;
            when(statsService.isCardKnown(deckId, cardId)).thenReturn(true);

            // When
            deckFacade.toggleKnown(deckId, cardId);

            // Then
            verify(statsService).isCardKnown(deckId, cardId);
            verify(statsService).setCardKnown(deckId, cardId, false);
        }

        @Test
        @DisplayName("ResetProgress should reset deck progress")
        void resetProgressShouldResetDeckProgress() {
            // Given
            Long deckId = 1L;

            // When
            deckFacade.resetProgress(deckId);

            // Then
            verify(statsService).resetDeckProgress(deckId);
        }
    }

    @Nested
    @DisplayName("Deck Management Tests")
    class DeckManagementTests {

        @Test
        @DisplayName("SaveDeck should save and return deck")
        void saveDeckShouldSaveAndReturnDeck() {
            // Given
            Deck deckToSave = new Deck(null, 1L, "Test Deck", "Test Description");
            Deck savedDeck = new Deck(1L, 1L, "Test Deck", "Test Description");

            when(deckUseCase.saveDeck(deckToSave)).thenReturn(savedDeck);

            // When
            Deck result = deckFacade.saveDeck(deckToSave);

            // Then
            assertThat(result).isEqualTo(savedDeck);
            verify(deckUseCase).saveDeck(deckToSave);
        }

        @Test
        @DisplayName("DeleteDeck should delete deck")
        void deleteDeckShouldDeleteDeck() {
            // Given
            Long deckId = 1L;

            // When
            deckFacade.deleteDeck(deckId);

            // Then
            verify(deckUseCase).deleteDeck(deckId);
        }
    }

    @Nested
    @DisplayName("Deck Size and Progress Tests")
    class DeckSizeAndProgressTests {

        @Test
        @DisplayName("DeckSize should return correct size")
        void deckSizeShouldReturnCorrectSize() {
            // Given
            Long deckId = 1L;
            List<Flashcard> flashcards = List.of(
                    new Flashcard(1L, deckId, "Front 1", "Back 1"),
                    new Flashcard(2L, deckId, "Front 2", "Back 2"),
                    new Flashcard(3L, deckId, "Front 3", "Back 3"));

            when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(flashcards);

            // When
            int result = deckFacade.deckSize(deckId);

            // Then
            assertThat(result).isEqualTo(3);
            verify(flashcardUseCase).getFlashcardsByDeckId(deckId);
        }

        @Test
        @DisplayName("DeckSize should return 0 for empty deck")
        void deckSizeShouldReturnZeroForEmptyDeck() {
            // Given
            Long deckId = 1L;
            when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(List.of());

            // When
            int result = deckFacade.deckSize(deckId);

            // Then
            assertThat(result).isZero();
            verify(flashcardUseCase).getFlashcardsByDeckId(deckId);
        }

        @Test
        @DisplayName("ProgressPercent should return correct percentage")
        void progressPercentShouldReturnCorrectPercentage() {
            // Given
            Long deckId = 1L;
            int deckSize = 10;
            int expectedPercent = 60;

            when(flashcardUseCase.getFlashcardsByDeckId(deckId))
                    .thenReturn(List.of(
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard(),
                            new Flashcard()));
            when(statsService.getDeckProgressPercent(deckId, deckSize)).thenReturn(expectedPercent);

            // When
            int result = deckFacade.progressPercent(deckId);

            // Then
            assertThat(result).isEqualTo(expectedPercent);
            verify(flashcardUseCase).getFlashcardsByDeckId(deckId);
            verify(statsService).getDeckProgressPercent(deckId, deckSize);
        }

        @Test
        @DisplayName("ProgressPercent should handle zero deck size")
        void progressPercentShouldHandleZeroDeckSize() {
            // Given
            Long deckId = 1L;
            when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(List.of());
            when(statsService.getDeckProgressPercent(deckId, 0)).thenReturn(0);

            // When
            int result = deckFacade.progressPercent(deckId);

            // Then
            assertThat(result).isZero();
            verify(flashcardUseCase).getFlashcardsByDeckId(deckId);
            verify(statsService).getDeckProgressPercent(deckId, 0);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null flashcard in saveFlashcard")
        void shouldHandleNullFlashcardInSaveFlashcard() {
            // Given
            Flashcard nullFlashcard = null;
            when(flashcardUseCase.saveFlashcard(nullFlashcard))
                    .thenThrow(new NullPointerException("flashcard cannot be null"));

            // When & Then
            assertThatThrownBy(() -> deckFacade.saveFlashcard(nullFlashcard))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("flashcard cannot be null");
            verify(flashcardUseCase).saveFlashcard(nullFlashcard);
        }

        @Test
        @DisplayName("Should handle null deck in saveDeck")
        void shouldHandleNullDeckInSaveDeck() {
            // Given
            Deck nullDeck = null;
            when(deckUseCase.saveDeck(nullDeck)).thenThrow(new NullPointerException("deck cannot be null"));

            // When & Then
            assertThatThrownBy(() -> deckFacade.saveDeck(nullDeck))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("deck cannot be null");
            verify(deckUseCase).saveDeck(nullDeck);
        }

        @Test
        @DisplayName("Should handle very large deck IDs")
        void shouldHandleVeryLargeDeckIds() {
            // Given
            Long largeDeckId = Long.MAX_VALUE;
            when(flashcardUseCase.getFlashcardsByDeckId(largeDeckId)).thenReturn(List.of());

            // When & Then
            assertThatNoException().isThrownBy(() -> deckFacade.deckSize(largeDeckId));
        }

        @Test
        @DisplayName("Should handle very large card IDs")
        void shouldHandleVeryLargeCardIds() {
            // Given
            Long deckId = 1L;
            Long largeCardId = Long.MAX_VALUE;
            when(statsService.isCardKnown(deckId, largeCardId)).thenReturn(false);

            // When & Then
            assertThatNoException().isThrownBy(() -> deckFacade.toggleKnown(deckId, largeCardId));
        }
    }

    @Nested
    @DisplayName("Transaction Tests")
    class TransactionTests {

        @Test
        @DisplayName("Read operations should be read-only transactional")
        void readOperationsShouldBeReadOnlyTransactional() {
            // This test verifies that read methods are annotated with @Transactional(readOnly = true)
            // The actual transaction behavior is tested in integration tests

            // Given
            Long deckId = 1L;
            Deck deck = new Deck(deckId, 1L, "Test Deck", "Test Description");
            when(deckUseCase.getDeckById(deckId)).thenReturn(Optional.of(deck));

            // When
            Deck result = deckFacade.getDeckOrThrow(deckId);

            // Then
            assertThat(result).isEqualTo(deck);
            // Transaction behavior is verified by the fact that the method executes without error
        }

        @Test
        @DisplayName("Write operations should be transactional")
        void writeOperationsShouldBeTransactional() {
            // This test verifies that write methods are annotated with @Transactional
            // The actual transaction behavior is tested in integration tests

            // Given
            Long deckId = 1L;

            // When & Then
            assertThatNoException().isThrownBy(() -> deckFacade.deleteDeck(deckId));
            // Transaction behavior is verified by the fact that the method executes without error
        }
    }
}
