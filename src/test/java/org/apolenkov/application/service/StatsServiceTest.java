package org.apolenkov.application.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.domain.port.StatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StatsServiceTest {

    private StatsService stats;
    private StatsRepository repo;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(StatsRepository.class);
        stats = new StatsService(repo);
    }

    @Test
    void recordSession_accumulatesAndProgress() {
        long deckId = 42L;
        stats.recordSession(deckId, 10, 7, 0, 3, Duration.ofMinutes(5), 2000, List.of(1L, 2L, 3L));
        stats.recordSession(deckId, 5, 3, 0, 2, Duration.ofMinutes(2), 1000, List.of(4L));

        Mockito.verify(repo, Mockito.times(2))
                .appendSession(
                        Mockito.eq(deckId),
                        Mockito.any(LocalDate.class),
                        Mockito.anyInt(),
                        Mockito.anyInt(),
                        Mockito.anyInt(),
                        Mockito.anyInt(),
                        Mockito.anyLong(),
                        Mockito.anyLong(),
                        Mockito.any());

        Mockito.when(repo.getDailyStats(deckId))
                .thenReturn(List.of(
                        new StatsRepository.DailyStatsRecord(LocalDate.now(), 2, 15, 10, 0, 5, 7 * 60_000L, 3000L)));
        Mockito.when(repo.getKnownCardIds(deckId)).thenReturn(Set.of(1L, 2L, 3L, 4L));

        var daily = stats.getDailyStatsForDeck(deckId);
        assertFalse(daily.isEmpty());
        int percent = stats.getDeckProgressPercent(deckId, 10);
        assertTrue(percent > 0);
        assertEquals(4, stats.getKnownCardIds(deckId).size());
    }
}
