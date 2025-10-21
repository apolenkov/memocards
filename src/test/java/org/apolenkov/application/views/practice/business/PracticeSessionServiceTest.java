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
import org.apolenkov.application.domain.usecase.CardUseCase;
import org.apolenkov.application.domain.usecase.DeckUseCase;
import org.apolenkov.application.model.Card;
import org.apolenkov.application.model.Deck;
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
    private CardUseCase cardUseCase;

    @Mock
    private StatsService statsService;

    @Mock
    private PracticeSettingsService practiceSettingsService;

    private PracticeSessionService sessionService;

    private List<Card> testCards;

    @Test
    @DisplayName("Should create service with valid dependencies")
    void shouldCreateServiceWithValidDependencies() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

        assertThat(sessionService).isNotNull();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null DeckUseCase")
    void shouldThrowExceptionForNullDeckUseCase() {
        assertThatThrownBy(() -> new PracticeSessionService(null, cardUseCase, statsService, practiceSettingsService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DeckUseCase cannot be null");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null CardUseCase")
    void shouldThrowExceptionForNullCardUseCase() {
        assertThatThrownBy(() -> new PracticeSessionService(deckUseCase, null, statsService, practiceSettingsService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CardUseCase cannot be null");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null StatsService")
    void shouldThrowExceptionForNullStatsService() {
        assertThatThrownBy(() -> new PracticeSessionService(deckUseCase, cardUseCase, null, practiceSettingsService))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("StatsService cannot be null");
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    @DisplayName("Should throw exception for null PracticeSettingsService")
    void shouldThrowExceptionForNullPracticeSettingsService() {
        assertThatThrownBy(() -> new PracticeSessionService(deckUseCase, cardUseCase, statsService, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PracticeSettingsService cannot be null");
    }

    @Test
    @DisplayName("Should load deck by ID")
    void shouldLoadDeckById() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        Deck testDeck = new Deck(1L, 1L, "Test Deck", "Test Description");

        when(deckUseCase.getDeckById(1L)).thenReturn(Optional.of(testDeck));

        Optional<Deck> result = sessionService.loadDeck(1L);

        assertThat(result).contains(testDeck);
    }

    @Test
    @DisplayName("Should throw exception for invalid deck ID")
    void shouldThrowExceptionForInvalidDeckId() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

        assertThatThrownBy(() -> sessionService.loadDeck(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deck ID must be positive, got: 0");
    }

    @Test
    @DisplayName("Should get not known cards")
    void shouldGetNotKnownCards() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of(1L));

        List<Card> result = sessionService.getNotKnownCards(1L);

        assertThat(result).hasSize(1).contains(testCards.get(1));
    }

    @Test
    @DisplayName("Should return all cards when none are known")
    void shouldReturnAllCardsWhenNoneAreKnown() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        List<Card> result = sessionService.getNotKnownCards(1L);

        assertThat(result).hasSize(2).isEqualTo(testCards);
    }

    @Test
    @DisplayName("Should resolve default count")
    void shouldResolveDefaultCount() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());
        when(practiceSettingsService.getDefaultCount()).thenReturn(10);

        int result = sessionService.resolveDefaultCount(1L);

        assertThat(result).isEqualTo(2); // Min of available cards and configured count
    }

    @Test
    @DisplayName("Should return configured count when more cards available")
    void shouldReturnConfiguredCountWhenMoreCardsAvailable() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Card(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Card(3L, 1L, "Front 3", "Back 3", "Example 3"),
                new Card(4L, 1L, "Front 4", "Back 4", "Example 4"),
                new Card(5L, 1L, "Front 5", "Back 5", "Example 5"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());
        when(practiceSettingsService.getDefaultCount()).thenReturn(3);

        int result = sessionService.resolveDefaultCount(1L);

        assertThat(result).isEqualTo(3); // Configured count
    }

    @Test
    @DisplayName("Should resolve default count from pre-loaded cards (optimized)")
    void shouldResolveDefaultCountFromPreLoadedCards() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        when(practiceSettingsService.getDefaultCount()).thenReturn(10);

        List<Card> notKnownCards = List.of(
                new Card(1L, 1L, "Front1", "Back1", "Example1"),
                new Card(2L, 1L, "Front2", "Back2", "Example2"),
                new Card(3L, 1L, "Front3", "Back3", "Example3"));

        // When: Using optimized overload
        int result = sessionService.resolveDefaultCount(notKnownCards);

        // Then: Count calculated without database query
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return random setting")
    void shouldReturnRandomSetting() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

        when(practiceSettingsService.isDefaultRandomOrder()).thenReturn(true);

        boolean result = sessionService.isRandom();

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return default direction")
    void shouldReturnDefaultDirection() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

        when(practiceSettingsService.getDefaultDirection()).thenReturn(PracticeDirection.BACK_TO_FRONT);

        PracticeDirection result = sessionService.defaultDirection();

        assertThat(result).isEqualTo(PracticeDirection.BACK_TO_FRONT);
    }

    @Test
    @DisplayName("Should return FRONT_TO_BACK when direction is null")
    void shouldReturnFrontToBackWhenDirectionIsNull() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

        when(practiceSettingsService.getDefaultDirection()).thenReturn(null);

        PracticeDirection result = sessionService.defaultDirection();

        assertThat(result).isEqualTo(PracticeDirection.FRONT_TO_BACK);
    }

    @Test
    @DisplayName("Should prepare session with random order")
    void shouldPrepareSessionWithRandomOrder() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Card(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Card(3L, 1L, "Front 3", "Back 3", "Example 3"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        List<Card> result = sessionService.prepareSession(1L, 2, true);

        assertThat(result).hasSize(2);
        // Note: We can't easily test randomization without mocking Collections.shuffle
    }

    @Test
    @DisplayName("Should prepare session with sequential order")
    void shouldPrepareSessionWithSequentialOrder() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Card(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Card(3L, 1L, "Front 3", "Back 3", "Example 3"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        List<Card> result = sessionService.prepareSession(1L, 2, false);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isEqualTo(testCards.get(0));
        assertThat(result.get(1)).isEqualTo(testCards.get(1));
    }

    @Test
    @DisplayName("Should return empty list when no unknown cards")
    void shouldReturnEmptyListWhenNoUnknownCards() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(new Card(1L, 1L, "Front 1", "Back 1", "Example 1"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of(1L));

        List<Card> result = sessionService.prepareSession(1L, 5, false);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should start session")
    void shouldStartSession() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(2L, 1L, "Front 2", "Back 2", "Example 2"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
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
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

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

    @Test
    @DisplayName("Should calculate completion metrics for session")
    void shouldCalculateCompletionMetrics() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Card(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Card(3L, 1L, "Front 3", "Back 3", "Example 3"));

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of());

        // Given: Completed session
        PracticeSession session = sessionService.startSession(1L, 3, false);

        // When: Calculate metrics (session duration calculated from timestamps)
        PracticeSessionService.SessionCompletionMetrics metrics = sessionService.calculateCompletionMetrics(session);

        // Then: Metrics calculated correctly
        assertThat(metrics.totalCards()).isEqualTo(3);
        assertThat(metrics.sessionMinutes()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.avgSeconds()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Should get failed cards from deck")
    void shouldGetFailedCards() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        testCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"),
                new Card(2L, 1L, "Front 2", "Back 2", "Example 2"),
                new Card(3L, 1L, "Front 3", "Back 3", "Example 3"));

        List<Long> failedCardIds = List.of(1L, 3L);

        when(cardUseCase.getCardsByDeckId(1L)).thenReturn(testCards);
        when(statsService.getKnownCardIds(1L)).thenReturn(Set.of(2L)); // Card 2 is known

        // When: Get failed cards
        List<Card> failedCards = sessionService.getFailedCards(1L, failedCardIds);

        // Then: Only cards that are both failed AND not known
        assertThat(failedCards).hasSize(2);
        assertThat(failedCards.get(0).getId()).isEqualTo(1L);
        assertThat(failedCards.get(1).getId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should return empty list when failed card IDs is null")
    void shouldReturnEmptyListWhenFailedCardIdsNull() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

        // When: Null failed card IDs
        List<Card> failedCards = sessionService.getFailedCards(1L, null);

        // Then: Empty list
        assertThat(failedCards).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when failed card IDs is empty")
    void shouldReturnEmptyListWhenFailedCardIdsEmpty() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);

        // When: Empty failed card IDs
        List<Card> failedCards = sessionService.getFailedCards(1L, List.of());

        // Then: Empty list
        assertThat(failedCards).isEmpty();
    }

    @Test
    @DisplayName("Should start repeat session with failed cards")
    void shouldStartRepeatSession() {
        sessionService = new PracticeSessionService(deckUseCase, cardUseCase, statsService, practiceSettingsService);
        List<Card> failedCards = List.of(
                new Card(1L, 1L, "Front 1", "Back 1", "Example 1"), new Card(3L, 1L, "Front 3", "Back 3", "Example 3"));

        // When: Start repeat session
        PracticeSession session = sessionService.startRepeatSession(1L, failedCards);

        // Then: Session created with failed cards (shuffled)
        assertThat(session.getDeckId()).isEqualTo(1L);
        assertThat(session.getCards()).hasSize(2);
        assertThat(session.getIndex()).isZero();
        assertThat(session.isShowingAnswer()).isFalse();
    }
}
