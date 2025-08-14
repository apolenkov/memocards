package org.apolenkov.application.views.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.service.DeckFacade;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.service.query.CardQueryService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeckPresenterTest {

    private DeckUseCase deckUseCase;
    private StatsService statsService;
    private DeckFacade deckFacade;
    private CardQueryService cardQueryService;
    private DeckPresenter presenter;

    @BeforeEach
    void setUp() {
        deckUseCase = mock(DeckUseCase.class);
        statsService = mock(StatsService.class);
        deckFacade = mock(DeckFacade.class);
        cardQueryService = mock(CardQueryService.class);
        presenter = new DeckPresenter(deckUseCase, statsService, deckFacade, cardQueryService);
    }

    @Test
    void loadDeck_returnsDeck() {
        when(deckUseCase.getDeckById(1L)).thenReturn(Optional.of(new Deck(1L, 100L, "t", null)));
        Optional<Deck> d = presenter.loadDeck(1L);
        assertThat(d).isPresent();
    }

    @Test
    void listFilteredFlashcards_filtersByQueryAndKnown() {
        long deckId = 10L;
        Flashcard a = new Flashcard();
        a.setId(1L);
        a.setFrontText("Hello");
        Flashcard b = new Flashcard();
        b.setId(2L);
        b.setFrontText("World");
        when(cardQueryService.listFilteredFlashcards(deckId, "he", true)).thenReturn(List.of(a));

        List<Flashcard> result = presenter.listFilteredFlashcards(deckId, "he", true);
        assertThat(result).extracting(Flashcard::getId).containsExactly(1L);
    }
}
