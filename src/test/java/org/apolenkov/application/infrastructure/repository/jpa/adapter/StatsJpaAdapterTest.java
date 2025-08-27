package org.apolenkov.application.infrastructure.repository.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apolenkov.application.domain.port.StatsRepository;
import org.apolenkov.application.infrastructure.repository.jpa.entity.DeckDailyStatsEntity;
import org.apolenkov.application.infrastructure.repository.jpa.entity.KnownCardEntity;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.KnownCardJpaRepository;
import org.apolenkov.application.infrastructure.repository.jpa.springdata.StatsJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsJpaAdapter Tests")
class StatsJpaAdapterTest {

    @Mock
    private StatsJpaRepository statsRepo;

    @Mock
    private KnownCardJpaRepository knownRepo;

    private StatsJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new StatsJpaAdapter(statsRepo, knownRepo);
    }

    @Nested
    @DisplayName("Profile Tests")
    class ProfileTests {
        @Test
        @DisplayName("Should be annotated with correct profile")
        void shouldBeAnnotatedWithCorrectProfile() {
            // Given
            Class<StatsJpaAdapter> clazz = StatsJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.context.annotation.Profile.class))
                    .isTrue();
            org.springframework.context.annotation.Profile profile =
                    clazz.getAnnotation(org.springframework.context.annotation.Profile.class);
            assertThat(profile.value()).contains("dev", "prod");
        }

        @Test
        @DisplayName("Should be annotated with Repository")
        void shouldBeAnnotatedWithRepository() {
            // Given
            Class<StatsJpaAdapter> clazz = StatsJpaAdapter.class;

            // When & Then
            assertThat(clazz.isAnnotationPresent(org.springframework.stereotype.Repository.class))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("Append Session Tests")
    class AppendSessionTests {
        @Test
        @DisplayName("AppendSession should accumulate existing stats when updated > 0")
        void appendSessionShouldAccumulateExistingStatsWhenUpdatedGreaterThanZero() {
            // Given
            long deckId = 1L;
            LocalDate date = LocalDate.now();
            int viewed = 10;
            int correct = 8;
            int repeat = 2;
            int hard = 1;
            long sessionDurationMs = 60000L;
            long totalAnswerDelayMs = 5000L;
            Collection<Long> knownCardIdsDelta = List.of(1L, 2L);

            when(statsRepo.accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong()))
                    .thenReturn(1);
            when(knownRepo.findKnownCardIds(deckId)).thenReturn(Set.of());

            // When
            adapter.appendSession(
                    deckId,
                    date,
                    viewed,
                    correct,
                    repeat,
                    hard,
                    sessionDurationMs,
                    totalAnswerDelayMs,
                    knownCardIdsDelta);

            // Then
            verify(statsRepo)
                    .accumulate(deckId, date, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs);
            verify(knownRepo).findKnownCardIds(deckId);
            // Since knownCardIdsDelta contains 2 cards and none exist, both should be saved
            verify(knownRepo, times(2)).save(any(KnownCardEntity.class));
        }

        @Test
        @DisplayName("AppendSession should create new stats when updated = 0")
        void appendSessionShouldCreateNewStatsWhenUpdatedEqualsZero() {
            // Given
            long deckId = 1L;
            LocalDate date = LocalDate.now();
            int viewed = 10;
            int correct = 8;
            int repeat = 2;
            int hard = 1;
            long sessionDurationMs = 60000L;
            long totalAnswerDelayMs = 5000L;

            when(statsRepo.accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong()))
                    .thenReturn(0);
            when(statsRepo.save(any(DeckDailyStatsEntity.class))).thenReturn(new DeckDailyStatsEntity());

            // When
            adapter.appendSession(
                    deckId, date, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs, null);

            // Then
            verify(statsRepo)
                    .accumulate(deckId, date, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs);
            verify(statsRepo).save(any(DeckDailyStatsEntity.class));
        }

        @Test
        @DisplayName("AppendSession should handle null knownCardIdsDelta")
        void appendSessionShouldHandleNullKnownCardIdsDelta() {
            // Given
            long deckId = 1L;
            LocalDate date = LocalDate.now();

            when(statsRepo.accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong()))
                    .thenReturn(1);

            // When
            adapter.appendSession(deckId, date, 10, 8, 2, 1, 60000L, 5000L, null);

            // Then
            verify(statsRepo)
                    .accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong());
        }

        @Test
        @DisplayName("AppendSession should handle empty knownCardIdsDelta")
        void appendSessionShouldHandleEmptyKnownCardIdsDelta() {
            // Given
            long deckId = 1L;
            LocalDate date = LocalDate.now();
            Collection<Long> knownCardIdsDelta = List.of();

            when(statsRepo.accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong()))
                    .thenReturn(1);

            // When
            adapter.appendSession(deckId, date, 10, 8, 2, 1, 60000L, 5000L, knownCardIdsDelta);

            // Then
            verify(statsRepo)
                    .accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong());
        }
    }

    @Nested
    @DisplayName("Get Daily Stats Tests")
    class GetDailyStatsTests {
        @Test
        @DisplayName("GetDailyStats should return daily stats for deck")
        void getDailyStatsShouldReturnDailyStatsForDeck() {
            // Given
            long deckId = 1L;
            LocalDate date1 = LocalDate.now().minusDays(1);
            LocalDate date2 = LocalDate.now();

            DeckDailyStatsEntity entity1 = createDeckDailyStatsEntity(deckId, date1, 2, 20, 16, 2, 1, 120000L, 10000L);
            DeckDailyStatsEntity entity2 = createDeckDailyStatsEntity(deckId, date2, 1, 10, 8, 1, 2, 60000L, 5000L);

            when(statsRepo.findById_DeckIdOrderById_DateAsc(deckId)).thenReturn(List.of(entity1, entity2));

            // When
            List<StatsRepository.DailyStatsRecord> result = adapter.getDailyStats(deckId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().date()).isEqualTo(date1);
            assertThat(result.getFirst().sessions()).isEqualTo(2);
            assertThat(result.getFirst().viewed()).isEqualTo(20);
            assertThat(result.get(1).date()).isEqualTo(date2);
            assertThat(result.get(1).sessions()).isEqualTo(1);
            verify(statsRepo).findById_DeckIdOrderById_DateAsc(deckId);
        }

        @Test
        @DisplayName("GetDailyStats should return empty list when no stats exist")
        void getDailyStatsShouldReturnEmptyListWhenNoStatsExist() {
            // Given
            long deckId = 1L;
            when(statsRepo.findById_DeckIdOrderById_DateAsc(deckId)).thenReturn(List.of());

            // When
            List<StatsRepository.DailyStatsRecord> result = adapter.getDailyStats(deckId);

            // Then
            assertThat(result).isEmpty();
            verify(statsRepo).findById_DeckIdOrderById_DateAsc(deckId);
        }
    }

    @Nested
    @DisplayName("Get Known Card IDs Tests")
    class GetKnownCardIdsTests {
        @Test
        @DisplayName("GetKnownCardIds should return known card IDs for deck")
        void getKnownCardIdsShouldReturnKnownCardIdsForDeck() {
            // Given
            long deckId = 1L;
            Set<Long> expectedCardIds = Set.of(1L, 2L, 3L);
            when(knownRepo.findKnownCardIds(deckId)).thenReturn(expectedCardIds);

            // When
            Set<Long> result = adapter.getKnownCardIds(deckId);

            // Then
            assertThat(result).isEqualTo(expectedCardIds);
            verify(knownRepo).findKnownCardIds(deckId);
        }

        @Test
        @DisplayName("GetKnownCardIds should return empty set when no known cards exist")
        void getKnownCardIdsShouldReturnEmptySetWhenNoKnownCardsExist() {
            // Given
            long deckId = 1L;
            when(knownRepo.findKnownCardIds(deckId)).thenReturn(Set.of());

            // When
            Set<Long> result = adapter.getKnownCardIds(deckId);

            // Then
            assertThat(result).isEmpty();
            verify(knownRepo).findKnownCardIds(deckId);
        }
    }

    @Nested
    @DisplayName("Set Card Known Tests")
    class SetCardKnownTests {
        @Test
        @DisplayName("SetCardKnown should add card when known = true and card not exists")
        void setCardKnownShouldAddCardWhenKnownTrueAndCardNotExists() {
            // Given
            long deckId = 1L;
            long cardId = 1L;
            boolean known = true;

            when(knownRepo.findKnownCardIds(deckId)).thenReturn(Set.of());
            when(knownRepo.save(any(KnownCardEntity.class))).thenReturn(new KnownCardEntity());

            // When
            adapter.setCardKnown(deckId, cardId, known);

            // Then
            verify(knownRepo).findKnownCardIds(deckId);
            verify(knownRepo).save(any(KnownCardEntity.class));
        }

        @Test
        @DisplayName("SetCardKnown should not add card when known = true and card already exists")
        void setCardKnownShouldNotAddCardWhenKnownTrueAndCardAlreadyExists() {
            // Given
            long deckId = 1L;
            long cardId = 1L;
            boolean known = true;

            when(knownRepo.findKnownCardIds(deckId)).thenReturn(Set.of(cardId));

            // When
            adapter.setCardKnown(deckId, cardId, known);

            // Then
            verify(knownRepo).findKnownCardIds(deckId);
            // Should not add or delete since card already exists, and we want it to be known
            verify(knownRepo, never()).save(any(KnownCardEntity.class));
            verify(knownRepo, never()).deleteKnown(anyLong(), anyLong());
        }

        @Test
        @DisplayName("SetCardKnown should delete card when known = false")
        void setCardKnownShouldDeleteCardWhenKnownFalse() {
            // Given
            long deckId = 1L;
            long cardId = 1L;
            boolean known = false;

            // When
            adapter.setCardKnown(deckId, cardId, known);

            // Then
            verify(knownRepo).deleteKnown(deckId, cardId);
        }
    }

    @Nested
    @DisplayName("Reset Deck Progress Tests")
    class ResetDeckProgressTests {
        @Test
        @DisplayName("ResetDeckProgress should delete all known cards for deck")
        void resetDeckProgressShouldDeleteAllKnownCardsForDeck() {
            // Given
            long deckId = 1L;

            // When
            adapter.resetDeckProgress(deckId);

            // Then
            verify(knownRepo).deleteByDeckId(deckId);
        }
    }

    @Nested
    @DisplayName("Get Aggregates For Decks Tests")
    class GetAggregatesForDecksTests {
        @Test
        @DisplayName("GetAggregatesForDecks should return aggregates for multiple decks")
        void getAggregatesForDecksShouldReturnAggregatesForMultipleDecks() {
            // Given
            Collection<Long> deckIds = List.of(1L, 2L);
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            Object[] row1 = {1L, today, 2, 20, 16, 1, 120000L, 10000L};
            Object[] row2 = {1L, yesterday, 1, 10, 8, 1, 60000L, 5000L};
            Object[] row3 = {2L, today, 1, 15, 12, 1, 90000L, 8000L};

            when(statsRepo.findAllByDeckIds(List.of(1L, 2L))).thenReturn(List.of(row1, row2, row3));

            // When
            Map<Long, StatsRepository.DeckAggregate> result = adapter.getAggregatesForDecks(deckIds, today);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(1L)).isNotNull();
            assertThat(result.get(2L)).isNotNull();
            verify(statsRepo).findAllByDeckIds(List.of(1L, 2L));
        }

        @Test
        @DisplayName("GetAggregatesForDecks should return empty map when deckIds is null")
        void getAggregatesForDecksShouldReturnEmptyMapWhenDeckIdsIsNull() {
            // Given
            LocalDate today = LocalDate.now();

            // When
            Map<Long, StatsRepository.DeckAggregate> result = adapter.getAggregatesForDecks(null, today);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("GetAggregatesForDecks should return empty map when deckIds is empty")
        void getAggregatesForDecksShouldReturnEmptyMapWhenDeckIdsIsEmpty() {
            // Given
            Collection<Long> deckIds = List.of();
            LocalDate today = LocalDate.now();

            // When
            Map<Long, StatsRepository.DeckAggregate> result = adapter.getAggregatesForDecks(deckIds, today);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("GetAggregatesForDecks should handle empty results from repository")
        void getAggregatesForDecksShouldHandleEmptyResultsFromRepository() {
            // Given
            Collection<Long> deckIds = List.of(1L, 2L);
            LocalDate today = LocalDate.now();

            when(statsRepo.findAllByDeckIds(List.of(1L, 2L))).thenReturn(List.of());

            // When
            Map<Long, StatsRepository.DeckAggregate> result = adapter.getAggregatesForDecks(deckIds, today);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(1L)).isNotNull();
            assertThat(result.get(2L)).isNotNull();
            verify(statsRepo).findAllByDeckIds(List.of(1L, 2L));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Should handle very large numbers in appendSession")
        void shouldHandleVeryLargeNumbersInAppendSession() {
            // Given
            long deckId = Long.MAX_VALUE;
            LocalDate date = LocalDate.now();
            int viewed = Integer.MAX_VALUE;
            int correct = Integer.MAX_VALUE;
            int repeat = Integer.MAX_VALUE;
            int hard = Integer.MAX_VALUE;
            long sessionDurationMs = Long.MAX_VALUE;
            long totalAnswerDelayMs = Long.MAX_VALUE;

            when(statsRepo.accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong()))
                    .thenReturn(1);

            // When
            adapter.appendSession(
                    deckId, date, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs, null);

            // Then
            verify(statsRepo)
                    .accumulate(deckId, date, viewed, correct, repeat, hard, sessionDurationMs, totalAnswerDelayMs);
        }

        @Test
        @DisplayName("Should handle very long knownCardIdsDelta")
        void shouldHandleVeryLongKnownCardIdsDelta() {
            // Given
            long deckId = 1L;
            LocalDate date = LocalDate.now();
            Collection<Long> knownCardIdsDelta = List.of(1L, 2L, 3L, 4L, 5L);

            when(statsRepo.accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong()))
                    .thenReturn(1);
            when(knownRepo.findKnownCardIds(deckId)).thenReturn(Set.of());

            // When
            adapter.appendSession(deckId, date, 10, 8, 2, 1, 60000L, 5000L, knownCardIdsDelta);

            // Then
            verify(statsRepo)
                    .accumulate(
                            anyLong(),
                            any(LocalDate.class),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyInt(),
                            anyLong(),
                            anyLong());
            verify(knownRepo).findKnownCardIds(deckId);
        }
    }

    @SuppressWarnings("ParameterNumber")
    private DeckDailyStatsEntity createDeckDailyStatsEntity(
            long deckId,
            LocalDate date,
            int sessions,
            int viewed,
            int correct,
            int repeat,
            int hard,
            long totalDurationMs,
            long totalAnswerDelayMs) {
        DeckDailyStatsEntity entity = new DeckDailyStatsEntity();
        DeckDailyStatsEntity.Id id = new DeckDailyStatsEntity.Id(deckId, date);
        entity.setId(id);
        entity.setSessions(sessions);
        entity.setViewed(viewed);
        entity.setCorrect(correct);
        entity.setRepeatCount(repeat);
        entity.setHard(hard);
        entity.setTotalDurationMs(totalDurationMs);
        entity.setTotalAnswerDelayMs(totalAnswerDelayMs);
        return entity;
    }
}
