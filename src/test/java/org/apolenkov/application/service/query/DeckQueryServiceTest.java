package org.apolenkov.application.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.apolenkov.application.usecase.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeckQueryServiceTest {

    private DeckUseCase deckUseCase;
    private FlashcardUseCase flashcardUseCase;
    private StatsService statsService;
    private UserUseCase userUseCase;
    private DeckQueryService service;

    @BeforeEach
    void setUp() {
        deckUseCase = mock(DeckUseCase.class);
        flashcardUseCase = mock(FlashcardUseCase.class);
        statsService = mock(StatsService.class);
        userUseCase = mock(UserUseCase.class);
        service = new DeckQueryService(deckUseCase, flashcardUseCase, statsService, userUseCase, null);
    }

    @Test
    void listDecksForCurrentUser_filtersByTitle() {
        when(userUseCase.getCurrentUser()).thenReturn(new org.apolenkov.application.model.User(1L, "u@e", "U"));
        Deck d1 = new Deck(1L, 1L, "Alpha", null);
        Deck d2 = new Deck(2L, 1L, "Beta", null);
        when(deckUseCase.getDecksByUserId(1L)).thenReturn(List.of(d1, d2));

        List<Deck> result = service.listDecksForCurrentUser("alp");

        assertThat(result).extracting(Deck::getTitle).containsExactly("Alpha");
    }
}
