package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Set;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeckFacadeProgressTest {

    private DeckUseCase deckUseCase;
    private FlashcardUseCase flashcardUseCase;
    private StatsService statsService;
    private jakarta.validation.Validator validator;
    private DeckFacade facade;

    @BeforeEach
    void setUp() {
        deckUseCase = mock(DeckUseCase.class);
        flashcardUseCase = mock(FlashcardUseCase.class);
        statsService = mock(StatsService.class);
        validator = mock(jakarta.validation.Validator.class);
        facade = new DeckFacade(deckUseCase, flashcardUseCase, statsService, validator);
    }

    @Test
    void progressPercent_usesStatsService() {
        long deckId = 7L;
        when(statsService.getDeckProgressPercent(deckId, 3)).thenReturn(66);
        when(flashcardUseCase.getFlashcardsByDeckId(deckId))
                .thenReturn(java.util.List.of(
                        new org.apolenkov.application.model.Flashcard(),
                        new org.apolenkov.application.model.Flashcard(),
                        new org.apolenkov.application.model.Flashcard()));

        int p = facade.progressPercent(deckId);

        assertThat(p).isEqualTo(66);
    }

    @Test
    void toggleKnown_flipsState() {
        long deckId = 4L;
        long cardId = 9L;
        when(statsService.isCardKnown(deckId, cardId)).thenReturn(false);

        facade.toggleKnown(deckId, cardId);

        verify(statsService).setCardKnown(deckId, cardId, true);
    }

    @Test
    void getKnown_delegates() {
        long deckId = 5L;
        when(statsService.getKnownCardIds(deckId)).thenReturn(Set.of(1L, 2L));
        assertThat(facade.getKnown(deckId)).containsExactlyInAnyOrder(1L, 2L);
    }
}
