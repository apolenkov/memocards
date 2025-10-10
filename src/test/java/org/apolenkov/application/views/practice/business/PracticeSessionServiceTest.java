package org.apolenkov.application.views.practice.business;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.domain.usecase.FlashcardUseCase;
import org.apolenkov.application.model.Deck;
import org.apolenkov.application.model.Flashcard;
import org.apolenkov.application.model.PracticeDirection;
import org.apolenkov.application.service.settings.PracticeSettingsService;
import org.apolenkov.application.service.stats.StatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PracticeSessionService Tests")
class PracticeSessionServiceTest {

    @Mock
    private DeckUseCase deckUseCase;

    @Mock
    private FlashcardUseCase flashcardUseCase;

    @Mock
    private StatsService statsService;

    @Mock
    private PracticeSettingsService practiceSettingsService;

    private PracticeSessionService sessionService;

    private List<Flashcard> testCards;

    @Test
    @DisplayName("Should create service with valid dependencies")
    void shouldCreateServiceWithValidDependencies() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);

        assertThat(sessionService).isNotNull();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null DeckUseCase")
    void shouldThrowExceptionForNullDeckUseCase() {
        assertThatThrownBy(
                        () -> new PracticeSessionService(null, flashcardUseCase, statsService, practiceSettingsService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DeckUseCase cannot be null");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null FlashcardUseCase")
    void shouldThrowExceptionForNullFlashcardUseCase() {
        assertThatThrownBy(() -> new PracticeSessionService(deckUseCase, null, statsService, practiceSettingsService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("FlashcardUseCase cannot be null");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null StatsService")
    void shouldThrowExceptionForNullStatsService() {
        assertThatThrownBy(
                        () -> new PracticeSessionService(deckUseCase, flashcardUseCase, null, practiceSettingsService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("StatsService cannot be null");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null PracticeSettingsService")
    void shouldThrowExceptionForNullPracticeSettingsService() {
        assertThatThrownBy(() -> new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PracticeSettingsService cannot be null");
    }

    @Test
    @DisplayName("Should load deck by ID")
    void shouldLoadDeckById() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        Deck testDeck = new Deck(1L, 1L, "Test Deck", "Test Description");

        when(deckUseCase.getDeckById(1L)).thenReturn(Optional.of(testDeck));

        Optional<Deck> result = sessionService.loadDeck(1L);

        assertThat(result).contains(testDeck);
    }

    @Test
    @DisplayName("Should throw exception for invalid deck ID")
    void shouldThrowExceptionForInvalidDeckId() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);

        assertThatThrownBy(() -> sessionService.loadDeck(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deck ID must be positive, got: 0");
    }

    @Test
    @DisplayName("Should get not known cards")
    void shouldGetNotKnownCards() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of(1L));

        List<Flashcard> result = sessionService.getNotKnownCards(1L);

        assertThat(result).hasSize(1).contains(testCards.get(1));
    }

    @Test
    @DisplayName("Should return all cards when none are known")
    void shouldReturnAllCardsWhenNoneAreKnown() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        List<Flashcard> result = sessionService.getNotKnownCards(1L);

        assertThat(result).hasSize(2).isEqualTo(testCards);
    }

    @Test
    @DisplayName("Should resolve default count")
    void shouldResolveDefaultCount() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());
        when(practiceSettingsService.getDefaultCount()).thenReturn(10);

        int result = sessionService.resolveDefaultCount(1L);

        assertThat(result).isEqualTo(2); // Min of available cards and configured count
    }

    @Test
    @DisplayName("Should return configured count when more cards available")
    void shouldReturnConfiguredCountWhenMoreCardsAvailable() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Flashcard(3L, 1L, "Front 3", "Back 3", "Example 3"),
                new Flashcard(4L, 1L, "Front 4", "Back 4", "Example 4"),
                new Flashcard(5L, 1L, "Front 5", "Back 5", "Example 5"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());
        when(practiceSettingsService.getDefaultCount()).thenReturn(3);

        int result = sessionService.resolveDefaultCount(1L);

        assertThat(result).isEqualTo(3); // Configured count
    }

    @Test
    @DisplayName("Should return random setting")
    void shouldReturnRandomSetting() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);

        when(practiceSettingsService.isDefaultRandomOrder()).thenReturn(true);

        boolean result = sessionService.isRandom();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return default direction")
    void shouldReturnDefaultDirection() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);

        when(practiceSettingsService.getDefaultDirection()).thenReturn(PracticeDirection.BACK_TO_FRONT);

        PracticeDirection result = sessionService.defaultDirection();

        assertThat(result).isEqualTo(PracticeDirection.BACK_TO_FRONT);
    }

    @Test
    @DisplayName("Should return FRONT_TO_BACK when direction is null")
    void shouldReturnFrontToBackWhenDirectionIsNull() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);

        when(practiceSettingsService.getDefaultDirection()).thenReturn(null);

        PracticeDirection result = sessionService.defaultDirection();

        assertThat(result).isEqualTo(PracticeDirection.FRONT_TO_BACK);
    }

    @Test
    @DisplayName("Should prepare session with random order")
    void shouldPrepareSessionWithRandomOrder() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Flashcard(3L, 1L, "Front 3", "Back 3", "Example 3"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        List<Flashcard> result = sessionService.prepareSession(1L, 2, true);

        assertThat(result).hasSize(2);
        // Note: We can't easily test randomization without mocking Collections.shuffle
    }

    @Test
    @DisplayName("Should prepare session with sequential order")
    void shouldPrepareSessionWithSequentialOrder() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Flashcard(3L, 1L, "Front 3", "Back 3", "Example 3"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        List<Flashcard> result = sessionService.prepareSession(1L, 2, false);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(testCards.get(0));
        assertThat(result.get(1)).isEqualTo(testCards.get(1));
    }

    @Test
    @DisplayName("Should return empty list when no unknown cards")
    void shouldReturnEmptyListWhenNoUnknownCards() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of(1L));

        List<Flashcard> result = sessionService.prepareSession(1L, 5, false);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should start session")
    void shouldStartSession() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Flashcard(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Flashcard(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(flashcardUseCase.getFlashcardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        PracticeSession result = sessionService.startSession(1L, 2, false);

        assertThat(result.getDeckId()).isEqualTo(1L);
        assertThat(result.getCards()).hasSize(2);
        assertThat(result.getIndex()).isZero();
        assertThat(result.isShowingAnswer()).isFalse();
    }

    @Test
    @DisplayName("Should record session")
    void shouldRecordSession() {
        sessionService =
                new PracticeSessionService(deckUseCase, flashcardUseCase, statsService, practiceSettingsService);

        List<Long> knownCardIds = List.of(1L, 2L);
        Duration sessionDuration = Duration.ofMinutes(5);
        long totalAnswerDelayMs = 30000L;

        sessionService.recordSession(1L, 10, 8, 2, sessionDuration, totalAnswerDelayMs, knownCardIds);

        ArgumentCaptor<SessionStatsDto> captor = ArgumentCaptor.forClass(SessionStatsDto.class);
        verify(statsService).recordSession(captor.capture());

        SessionStatsDto recorded = captor.getValue();
        assertThat(recorded.deckId()).isEqualTo(1L);
        assertThat(recorded.viewed()).isEqualTo(10);
        assertThat(recorded.correct()).isEqualTo(8);
        assertThat(recorded.hard()).isEqualTo(2);
        assertThat(recorded.sessionDurationMs()).isEqualTo(sessionDuration.toMillis());
        assertThat(recorded.totalAnswerDelayMs()).isEqualTo(totalAnswerDelayMs);
        assertThat(recorded.knownCardIdsDelta()).isEqualTo(knownCardIds);
    }
}
