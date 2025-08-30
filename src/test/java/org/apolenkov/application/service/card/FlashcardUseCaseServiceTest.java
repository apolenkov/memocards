package org.apolenkov.application.service.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlashcardUseCase Core Tests")
class FlashcardUseCaseServiceTest {

    @Mock
    private FlashcardUseCase flashcardUseCase;

    private List<Flashcard> testFlashcards;

    @BeforeEach
    void setUp() {
        testFlashcards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"));
    }

    @Test
    @DisplayName("Should get flashcards by deck id")
    void shouldGetFlashcardsByDeckId() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testFlashcards);

        List<Flashcard> result = flashcardUseCase.getFlashcardsByDeckId(1L);

        assertThat(result).hasSize(2).isEqualTo(testFlashcards);
    }

    @Test
    @DisplayName("Should handle empty flashcard list")
    void shouldHandleEmptyFlashcardList() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(List.of());

        List<Flashcard> result = flashcardUseCase.getFlashcardsByDeckId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get first flashcard")
    void shouldGetFirstFlashcard() {
        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testFlashcards);

        List<Flashcard> result = flashcardUseCase.getFlashcardsByDeckId(1L);
        Flashcard firstCard = result.getFirst();

        assertThat(firstCard).isEqualTo(testFlashcards.getFirst());
    }
}
