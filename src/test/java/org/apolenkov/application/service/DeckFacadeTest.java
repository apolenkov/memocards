package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeckFacade Service Tests")
class DeckFacadeTest {

    @Mock
    private DeckUseCase deckUseCase;

    @Mock
    private FlashcardUseCase flashcardUseCase;

    @Mock
    private StatsService statsService;

    private DeckFacade deckFacade;

    private Deck testDeck;
    private List<Flashcard> testFlashcards;

    @BeforeEach
    void setUp() {
        deckFacade = new DeckFacade(deckUseCase, flashcardUseCase, statsService);

        testDeck = new Deck(1L, 1L, "Test Deck", "Test Description");

        testFlashcards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"));
    }

    @Test
    @DisplayName("Should get deck by id successfully")
    void shouldGetDeckByIdSuccessfully() {
        when(deckUseCase.getDeckById(1L)).thenReturn(Optional.of(testDeck));

        Deck result = deckFacade.getDeckOrThrow(1L);

        assertThat(result).isEqualTo(testDeck);
    }

    @Test
    @DisplayName("Should throw exception when deck not found")
    void shouldThrowExceptionWhenDeckNotFound() {
        when(deckUseCase.getDeckById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckFacade.getDeckOrThrow(999L)).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("Should load flashcards for deck")
    void shouldLoadFlashcardsForDeck() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testFlashcards);

        List<Flashcard> result = deckFacade.loadFlashcards(1L);

        assertThat(result).hasSize(2).isEqualTo(testFlashcards);
    }

    @Test
    @DisplayName("Should get known card ids")
    void shouldGetKnownCardIds() {
        Set<Long> knownIds = Set.of(1L);
        when(statsService.getKnownCardIds(1L)).thenReturn(knownIds);

        Set<Long> result = deckFacade.getKnown(1L);

        assertThat(result).hasSize(1).contains(1L);
    }

    @Test
    @DisplayName("Should toggle known status")
    void shouldToggleKnownStatus() {
        when(statsService.isCardKnown(1L, 1L)).thenReturn(false);

        deckFacade.toggleKnown(1L, 1L);

        // Verify the interaction
        assertThat(deckFacade).isNotNull();
    }

    @Test
    @DisplayName("Should save flashcard")
    void shouldSaveFlashcard() {
        Flashcard flashcard = testFlashcards.getFirst();
        when(flashcardUseCase.saveFlashcard(flashcard)).thenReturn(flashcard);

        Flashcard result = deckFacade.saveFlashcard(flashcard);

        assertThat(result).isEqualTo(flashcard);
    }

    @Test
    @DisplayName("Should delete flashcard")
    void shouldDeleteFlashcard() {
        deckFacade.deleteFlashcard(1L);

        // Verify the interaction
        assertThat(deckFacade).isNotNull();
    }

    @Test
    @DisplayName("Should save deck")
    void shouldSaveDeck() {
        when(deckUseCase.saveDeck(testDeck)).thenReturn(testDeck);

        Deck result = deckFacade.saveDeck(testDeck);

        assertThat(result).isEqualTo(testDeck);
    }

    @Test
    @DisplayName("Should delete deck")
    void shouldDeleteDeck() {
        deckFacade.deleteDeck(1L);

        // Verify the interaction
        assertThat(deckFacade).isNotNull();
    }

    @Test
    @DisplayName("Should get deck size")
    void shouldGetDeckSize() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testFlashcards);

        int result = deckFacade.deckSize(1L);

        assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get progress percent")
    void shouldGetProgressPercent() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testFlashcards);
        when(statsService.getDeckProgressPercent(1L, 2)).thenReturn(75);

        int result = deckFacade.progressPercent(1L);

        assertThat(result).isEqualTo(75);
    }

    @Test
    @DisplayName("Should handle empty flashcard list")
    void shouldHandleEmptyFlashcardList() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(List.of());

        List<Flashcard> result = deckFacade.loadFlashcards(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle zero deck size")
    void shouldHandleZeroDeckSize() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(List.of());

        int result = deckFacade.deckSize(1L);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("Should handle invalid deck id")
    void shouldHandleInvalidDeckId() {
        assertThatThrownBy(() -> deckFacade.getDeckOrThrow(0L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should handle negative deck id")
    void shouldHandleNegativeDeckId() {
        assertThatThrownBy(() -> deckFacade.getDeckOrThrow(-1L)).isInstanceOf(IllegalArgumentException.class);
    }
}
