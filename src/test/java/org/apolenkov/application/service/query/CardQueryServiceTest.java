package org.apolenkov.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardQueryServiceTest {

    private FlashcardUseCase flashcardUseCase;
    private StatsService statsService;
    private CardQueryService service;

    @BeforeEach
    void setUp() {
        flashcardUseCase = mock(FlashcardUseCase.class);
        statsService = mock(StatsService.class);
        service = new CardQueryService(flashcardUseCase, statsService);
    }

    @Test
    void listFilteredFlashcards_filtersByQuery_and_HidesKnown() {
        long deckId = 5L;
        Flashcard a = new Flashcard();
        a.setId(1L);
        a.setDeckId(deckId);
        a.setFrontText("Hello");
        Flashcard b = new Flashcard();
        b.setId(2L);
        b.setDeckId(deckId);
        b.setFrontText("World");
        when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(List.of(a, b));
        when(statsService.getKnownCardIds(deckId)).thenReturn(Set.of(2L));

        List<Flashcard> result = service.listFilteredFlashcards(deckId, "he", true);

        assertThat(result).extracting(Flashcard::getId).containsExactly(1L);
    }

    @Test
    void filterFlashcards_returnsAll_whenNoQueryAndShowKnown() {
        Flashcard a = new Flashcard();
        a.setId(1L);
        a.setFrontText("A");
        Flashcard b = new Flashcard();
        b.setId(2L);
        b.setFrontText("B");

        List<Flashcard> result = service.filterFlashcards(List.of(a, b), null, Set.of(), false);

        assertThat(result).hasSize(2);
    }
}
