package org.apolenkov.application.views.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.service.StatsService;
import org.apolenkov.application.usecase.DeckUseCase;
import org.apolenkov.application.usecase.FlashcardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PracticePresenterTest {

    private DeckUseCase deckUseCase;
    private FlashcardUseCase flashcardUseCase;
    private StatsService statsService;
    private PracticeSettingsService settings;
    private PracticePresenter presenter;

    @BeforeEach
    void setUp() {
        deckUseCase = mock(DeckUseCase.class);
        flashcardUseCase = mock(FlashcardUseCase.class);
        statsService = mock(StatsService.class);
        settings = mock(PracticeSettingsService.class);
        when(settings.getDefaultCount()).thenReturn(10);
        when(settings.isDefaultRandomOrder()).thenReturn(false);
        when(settings.getDefaultDirection()).thenReturn(PracticeDirection.FRONT_TO_BACK);
        presenter = new PracticePresenter(deckUseCase, flashcardUseCase, statsService, settings);
    }

    @Test
    void startSession_and_progress_flow() {
        long deckId = 5L;
        Flashcard c1 = new Flashcard();
        c1.setId(1L);
        c1.setFrontText("Hi");
        c1.setBackText("Привет");
        Flashcard c2 = new Flashcard();
        c2.setId(2L);
        c2.setFrontText("Cat");
        c2.setBackText("Кот");
        when(flashcardUseCase.getFlashcardsByDeckId(deckId)).thenReturn(List.of(c1, c2));
        when(statsService.getKnownCardIds(deckId)).thenReturn(java.util.Set.of());

        var s = presenter.startSession(deckId, 10, false, PracticeDirection.FRONT_TO_BACK);
        assertThat(s.cards).hasSize(2);
        presenter.startQuestion(s);
        assertThat(presenter.currentCard(s).getId()).isEqualTo(1L);
        presenter.reveal(s);
        presenter.markKnow(s);
        presenter.startQuestion(s);
        presenter.reveal(s);
        presenter.markHard(s);
        assertThat(presenter.isComplete(s)).isTrue();
        var prog = presenter.progress(s);
        assertThat(prog.totalViewed()).isEqualTo(2);
    }
}
