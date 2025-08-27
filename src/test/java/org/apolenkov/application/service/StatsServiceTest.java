package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.port.StatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsService Tests")
class StatsServiceTest {

    @Mock
    private StatsRepository statsRepository;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(statsRepository);
    }

    @Nested
    @DisplayName("Record Session Tests")
    class RecordSessionTests {

        @Test
        @DisplayName("RecordSession should call repository when viewed > 0")
        void recordSessionShouldCallRepositoryWhenViewedGreaterThanZero() {
            // Given
            long deckId = 1L;
            int viewed = 5;
            int correct = 3;
            int repeat = 1;
            int hard = 1;
            Duration sessionDuration = Duration.ofMinutes(10);
            long totalAnswerDelayMs = 5000L;
            Collection<Long> knownCardIdsDelta = Set.of(1L, 2L);

            // When
            statsService.recordSession(
                    deckId, viewed, correct, repeat, hard, sessionDuration, totalAnswerDelayMs, knownCardIdsDelta);

            // Then
            verify(statsRepository)
                    .appendSession(
                            deckId,
                            LocalDate.now(),
                            viewed,
                            correct,
                            repeat,
                            hard,
                            sessionDuration.toMillis(),
                            totalAnswerDelayMs,
                            knownCardIdsDelta);
        }

        @Test
        @DisplayName("RecordSession should not call repository when viewed <= 0")
        void recordSessionShouldNotCallRepositoryWhenViewedLessThanOrEqualToZero() {
            // Given
            long deckId = 1L;
            int viewed = 0;
            int correct = 0;
            int repeat = 0;
            int hard = 0;
            Duration sessionDuration = Duration.ofMinutes(10);
            long totalAnswerDelayMs = 0L;
            Collection<Long> knownCardIdsDelta = Set.of();

            // When
            statsService.recordSession(
                    deckId, viewed, correct, repeat, hard, sessionDuration, totalAnswerDelayMs, knownCardIdsDelta);

            // Then
            verifyNoInteractions(statsRepository);
        }

        @Test
        @DisplayName("RecordSession should handle negative viewed value")
        void recordSessionShouldHandleNegativeViewedValue() {
            // Given
            long deckId = 1L;
            int viewed = -5;
            int correct = 0;
            int repeat = 0;
            int hard = 0;
            Duration sessionDuration = Duration.ofMinutes(10);
            long totalAnswerDelayMs = 0L;
            Collection<Long> knownCardIdsDelta = Set.of();

            // When
            statsService.recordSession(
                    deckId, viewed, correct, repeat, hard, sessionDuration, totalAnswerDelayMs, knownCardIdsDelta);

            // Then
            verifyNoInteractions(statsRepository);
        }
    }

    @Nested
    @DisplayName("Get Daily Stats Tests")
    class GetDailyStatsTests {

        @Test
        @DisplayName("GetDailyStatsForDeck should return sorted daily stats")
        void getDailyStatsForDeckShouldReturnSortedDailyStats() {
            // Given
            long deckId = 1L;
            var mockStats1 = mock(StatsRepository.DailyStatsRecord.class);
            var mockStats2 = mock(StatsRepository.DailyStatsRecord.class);
            var mockStats3 = mock(StatsRepository.DailyStatsRecord.class);

            when(mockStats1.date()).thenReturn(LocalDate.of(2024, 1, 3));
            when(mockStats2.date()).thenReturn(LocalDate.of(2024, 1, 1));
            when(mockStats3.date()).thenReturn(LocalDate.of(2024, 1, 2));

            when(statsRepository.getDailyStats(deckId)).thenReturn(List.of(mockStats1, mockStats2, mockStats3));

            // When
            List<StatsService.DailyStats> result = statsService.getDailyStatsForDeck(deckId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.getFirst().date()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2024, 1, 2));
            assertThat(result.get(2).date()).isEqualTo(LocalDate.of(2024, 1, 3));
        }

        @Test
        @DisplayName("GetDailyStatsForDeck should return empty list when no stats")
        void getDailyStatsForDeckShouldReturnEmptyListWhenNoStats() {
            // Given
            long deckId = 1L;
            when(statsRepository.getDailyStats(deckId)).thenReturn(List.of());

            // When
            List<StatsService.DailyStats> result = statsService.getDailyStatsForDeck(deckId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("DailyStats record should calculate average delay correctly")
        void dailyStatsRecordShouldCalculateAverageDelayCorrectly() {
            // Given
            var dailyStats = new StatsService.DailyStats(
                    LocalDate.now(),
                    10, // sessions
                    5, // viewed
                    4, // correct
                    1, // repeat
                    0, // hard
                    600000L, // totalDurationMs (10 minutes)
                    25000L // totalAnswerDelayMs
                    );

            // When
            double avgDelay = dailyStats.getAvgDelayMs();

            // Then
            assertThat(avgDelay).isEqualTo(5000.0); // 25000 / 5
        }

        @Test
        @DisplayName("DailyStats record should return 0.0 when viewed is 0")
        void dailyStatsRecordShouldReturnZeroWhenViewedIsZero() {
            // Given
            var dailyStats = new StatsService.DailyStats(
                    LocalDate.now(),
                    10, // sessions
                    0, // viewed
                    0, // correct
                    0, // repeat
                    0, // hard
                    600000L, // totalDurationMs
                    0L // totalAnswerDelayMs
                    );

            // When
            double avgDelay = dailyStats.getAvgDelayMs();

            // Then
            assertThat(avgDelay).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Progress Tests")
    class ProgressTests {

        @Test
        @DisplayName("GetDeckProgressPercent should return correct percentage")
        void getDeckProgressPercentShouldReturnCorrectPercentage() {
            // Given
            long deckId = 1L;
            int deckSize = 20;
            Set<Long> knownCardIds = Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L); // 10 cards

            when(statsRepository.getKnownCardIds(deckId)).thenReturn(knownCardIds);

            // When
            int result = statsService.getDeckProgressPercent(deckId, deckSize);

            // Then
            assertThat(result).isEqualTo(50); // 10/20 * 100 = 50%
        }

        @Test
        @DisplayName("GetDeckProgressPercent should return 0 when deck size is 0")
        void getDeckProgressPercentShouldReturnZeroWhenDeckSizeIsZero() {
            // Given
            long deckId = 1L;
            int deckSize = 0;

            // When
            int result = statsService.getDeckProgressPercent(deckId, deckSize);

            // Then
            assertThat(result).isZero();
            verifyNoInteractions(statsRepository);
        }

        @Test
        @DisplayName("GetDeckProgressPercent should return 0 when deck size is negative")
        void getDeckProgressPercentShouldReturnZeroWhenDeckSizeIsNegative() {
            // Given
            long deckId = 1L;
            int deckSize = -5;

            // When
            int result = statsService.getDeckProgressPercent(deckId, deckSize);

            // Then
            assertThat(result).isZero();
            verifyNoInteractions(statsRepository);
        }

        @Test
        @DisplayName("GetDeckProgressPercent should return 100 when all cards are known")
        void getDeckProgressPercentShouldReturnHundredWhenAllCardsAreKnown() {
            // Given
            long deckId = 1L;
            int deckSize = 5;
            Set<Long> knownCardIds = Set.of(1L, 2L, 3L, 4L, 5L); // All 5 cards

            when(statsRepository.getKnownCardIds(deckId)).thenReturn(knownCardIds);

            // When
            int result = statsService.getDeckProgressPercent(deckId, deckSize);

            // Then
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("GetDeckProgressPercent should round correctly")
        void getDeckProgressPercentShouldRoundCorrectly() {
            // Given
            long deckId = 1L;
            int deckSize = 3;
            Set<Long> knownCardIds = Set.of(1L); // 1 out of 3 cards = 33.33...%

            when(statsRepository.getKnownCardIds(deckId)).thenReturn(knownCardIds);

            // When
            int result = statsService.getDeckProgressPercent(deckId, deckSize);

            // Then
            assertThat(result).isEqualTo(33); // Rounded down
        }
    }

    @Nested
    @DisplayName("Card Knowledge Tests")
    class CardKnowledgeTests {

        @Test
        @DisplayName("IsCardKnown should return true when card is known")
        void isCardKnownShouldReturnTrueWhenCardIsKnown() {
            // Given
            long deckId = 1L;
            long cardId = 5L;
            Set<Long> knownCardIds = Set.of(1L, 2L, 3L, 4L, 5L);

            when(statsRepository.getKnownCardIds(deckId)).thenReturn(knownCardIds);

            // When
            boolean result = statsService.isCardKnown(deckId, cardId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("IsCardKnown should return false when card is not known")
        void isCardKnownShouldReturnFalseWhenCardIsNotKnown() {
            // Given
            long deckId = 1L;
            long cardId = 10L;
            Set<Long> knownCardIds = Set.of(1L, 2L, 3L, 4L, 5L);

            when(statsRepository.getKnownCardIds(deckId)).thenReturn(knownCardIds);

            // When
            boolean result = statsService.isCardKnown(deckId, cardId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("GetKnownCardIds should return repository result")
        void getKnownCardIdsShouldReturnRepositoryResult() {
            // Given
            long deckId = 1L;
            Set<Long> expectedKnownCardIds = Set.of(1L, 2L, 3L);

            when(statsRepository.getKnownCardIds(deckId)).thenReturn(expectedKnownCardIds);

            // When
            Set<Long> result = statsService.getKnownCardIds(deckId);

            // Then
            assertThat(result).isEqualTo(expectedKnownCardIds);
        }
    }

    @Nested
    @DisplayName("Card Knowledge Management Tests")
    class CardKnowledgeManagementTests {

        @Test
        @DisplayName("SetCardKnown should call repository")
        void setCardKnownShouldCallRepository() {
            // Given
            long deckId = 1L;
            long cardId = 5L;
            boolean known = true;

            // When
            statsService.setCardKnown(deckId, cardId, known);

            // Then
            verify(statsRepository).setCardKnown(deckId, cardId, known);
        }

        @Test
        @DisplayName("ResetDeckProgress should call repository")
        void resetDeckProgressShouldCallRepository() {
            // Given
            long deckId = 1L;

            // When
            statsService.resetDeckProgress(deckId);

            // Then
            verify(statsRepository).resetDeckProgress(deckId);
        }
    }

    @Nested
    @DisplayName("Deck Aggregates Tests")
    class DeckAggregatesTests {

        @Test
        @DisplayName("GetDeckAggregates should return repository result")
        void getDeckAggregatesShouldReturnRepositoryResult() {
            // Given
            List<Long> deckIds = List.of(1L, 2L, 3L);
            LocalDate today = LocalDate.now();
            Map<Long, StatsRepository.DeckAggregate> expectedAggregates = Map.of(
                    1L, mock(StatsRepository.DeckAggregate.class),
                    2L, mock(StatsRepository.DeckAggregate.class),
                    3L, mock(StatsRepository.DeckAggregate.class));

            when(statsRepository.getAggregatesForDecks(deckIds, today)).thenReturn(expectedAggregates);

            // When
            Map<Long, StatsRepository.DeckAggregate> result = statsService.getDeckAggregates(deckIds, today);

            // Then
            assertThat(result).isEqualTo(expectedAggregates);
        }

        @Test
        @DisplayName("GetDeckAggregates should handle empty deck ids list")
        void getDeckAggregatesShouldHandleEmptyDeckIdsList() {
            // Given
            List<Long> deckIds = List.of();
            LocalDate today = LocalDate.now();
            Map<Long, StatsRepository.DeckAggregate> expectedAggregates = Map.of();

            when(statsRepository.getAggregatesForDecks(deckIds, today)).thenReturn(expectedAggregates);

            // When
            Map<Long, StatsRepository.DeckAggregate> result = statsService.getDeckAggregates(deckIds, today);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large numbers in recordSession")
        void shouldHandleVeryLargeNumbersInRecordSession() {
            // Given
            long deckId = Long.MAX_VALUE;
            int viewed = Integer.MAX_VALUE;
            int correct = Integer.MAX_VALUE;
            int repeat = Integer.MAX_VALUE;
            int hard = Integer.MAX_VALUE;
            Duration sessionDuration = Duration.ofDays(365);
            long totalAnswerDelayMs = Long.MAX_VALUE;
            Collection<Long> knownCardIdsDelta = Set.of(Long.MAX_VALUE);

            // When & Then
            assertThatNoException()
                    .isThrownBy(() -> statsService.recordSession(
                            deckId,
                            viewed,
                            correct,
                            repeat,
                            hard,
                            sessionDuration,
                            totalAnswerDelayMs,
                            knownCardIdsDelta));
        }

        @Test
        @DisplayName("Should handle zero duration in recordSession")
        void shouldHandleZeroDurationInRecordSession() {
            // Given
            long deckId = 1L;
            int viewed = 5;
            int correct = 3;
            int repeat = 1;
            int hard = 1;
            Duration sessionDuration = Duration.ZERO;
            long totalAnswerDelayMs = 0L;
            Collection<Long> knownCardIdsDelta = Set.of();

            // When
            statsService.recordSession(
                    deckId, viewed, correct, repeat, hard, sessionDuration, totalAnswerDelayMs, knownCardIdsDelta);

            // Then
            verify(statsRepository)
                    .appendSession(
                            deckId,
                            LocalDate.now(),
                            viewed,
                            correct,
                            repeat,
                            hard,
                            0L, // Duration.ZERO.toMillis() = 0
                            totalAnswerDelayMs,
                            knownCardIdsDelta);
        }
    }
}
