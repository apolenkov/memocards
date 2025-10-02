package org.apolenkov.application.views.presenter.practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.views.practice.business.PracticePresenter;
import org.apolenkov.application.views.practice.business.PracticeSessionManager;
import org.apolenkov.application.views.practice.business.PracticeSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PracticePresenter Core Tests")
class PracticePresenterTest {

    @Mock
    private PracticeSessionService practiceSessionService;

    @Mock
    private PracticeSessionManager practiceSessionManager;

    private PracticePresenter practicePresenter;

    private Deck testDeck;
    private List<Flashcard> testFlashcards;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        practicePresenter = new PracticePresenter(practiceSessionService, practiceSessionManager);

        testDeck = new Deck(1L, 1L, "Test Deck", "Test Description");

        testFlashcards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"));
    }

    @Test
    @DisplayName("Should load deck by ID")
    void shouldLoadDeckById() {
        when(practiceSessionService.loadDeck(1L)).thenReturn(Optional.of(testDeck));

        Optional<Deck> result = practicePresenter.loadDeck(1L);

        assertThat(result).contains(testDeck);
    }

    @Test
    @DisplayName("Should return empty for non-existent deck")
    void shouldReturnEmptyForNonExistentDeck() {
        when(practiceSessionService.loadDeck(999L)).thenReturn(Optional.empty());

        Optional<Deck> result = practicePresenter.loadDeck(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should get not known cards")
    void shouldGetNotKnownCards() {
        when(practiceSessionService.getNotKnownCards(1L)).thenReturn(testFlashcards);

        List<Flashcard> result = practicePresenter.getNotKnownCards(1L);

        assertThat(result).hasSize(2).isEqualTo(testFlashcards);
    }

    @Test
    @DisplayName("Should filter out known cards")
    void shouldFilterOutKnownCards() {
        List<Flashcard> filteredCards = List.of(testFlashcards.get(1));
        when(practiceSessionService.getNotKnownCards(1L)).thenReturn(filteredCards);

        List<Flashcard> result = practicePresenter.getNotKnownCards(1L);

        assertThat(result).hasSize(1).contains(testFlashcards.get(1));
    }

    @Test
    @DisplayName("Should handle empty deck")
    void shouldHandleEmptyDeck() {
        when(practiceSessionService.getNotKnownCards(1L)).thenReturn(List.of());

        List<Flashcard> result = practicePresenter.getNotKnownCards(1L);

        assertThat(result).isEmpty();
    }
}
