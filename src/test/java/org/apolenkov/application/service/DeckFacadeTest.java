package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeckFacadeTest {

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
    void loadFlashcards_delegatesToUseCase() {
        long deckId = 10L;
        Flashcard f = new Flashcard();
        f.setId(1L);
        when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(List.of(f));

        List<Flashcard> list = facade.loadFlashcards(deckId);

        assertThat(list).extracting(Flashcard::getId).containsExactly(1L);
    }
}
