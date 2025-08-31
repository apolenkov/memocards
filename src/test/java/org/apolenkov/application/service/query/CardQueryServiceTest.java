package org.apolenkov.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardQueryService Core Tests")
class CardQueryServiceTest {

    @Mock
    private FlashcardUseCase flashcardUseCase;

    @Mock
    private StatsService statsService;

    private CardQueryService cardQueryService;

    @BeforeEach
    void setUp() {
        cardQueryService = new CardQueryService(flashcardUseCase, statsService);
    }

    @Test
    @DisplayName("Should get filtered flashcards by deck id")
    void shouldGetFilteredFlashcardsByDeckId() {
        // Given
        Long deckId = 1L;
        List<Flashcard> expectedFlashcards =
                List.of(new Flashcard(1L, deckId, "Front 1", "Back 1"), new Flashcard(2L, deckId, "Front 2", "Back 2"));
        when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(expectedFlashcards);
        when(statsService.getKnownCardIds(deckId)).thenReturn(Set.of());

        // When
        List<Flashcard> result = cardQueryService.listFilteredFlashcards(deckId, null, false);

        // Then
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedFlashcards);
    }

    @Test
    @DisplayName("Should handle empty flashcard list")
    void shouldHandleEmptyFlashcardList() {
        // Given
        Long deckId = 1L;
        when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(List.of());
        when(statsService.getKnownCardIds(deckId)).thenReturn(Set.of());

        // When
        List<Flashcard> result = cardQueryService.listFilteredFlashcards(deckId, null, false);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should filter flashcards by search query")
    void shouldFilterFlashcardsBySearchQuery() {
        // Given
        Long deckId = 1L;
        List<Flashcard> allFlashcards = List.of(
                new Flashcard(1L, deckId, "Front 1", "Back 1"),
                new Flashcard(2L, deckId, "Front 2", "Back 2"),
                new Flashcard(3L, deckId, "Different", "Content"));
        when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(allFlashcards);
        when(statsService.getKnownCardIds(deckId)).thenReturn(Set.of());

        // When
        List<Flashcard> result = cardQueryService.listFilteredFlashcards(deckId, "Front", false);

        // Then
        assertThat(result).hasSize(2).allMatch(fc -> fc.getFrontText().contains("Front"));
    }
}
