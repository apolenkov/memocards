package org.apolenkov.application.service;

import org.apolenkov.application.infrastructure.repository.memory.InMemoryStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class StatsServiceTest {

    private StatsService stats;

    @BeforeEach
    void setUp() {
        stats = new StatsService(new InMemoryStatsRepository());
    }

    @Test
    void recordSession_accumulatesAndProgress() {
        long deckId = 42L;
        stats.recordSession(deckId, 10, 7, 0, 3, Duration.ofMinutes(5), 2000, java.util.List.of(1L, 2L, 3L));
        stats.recordSession(deckId, 5, 3, 0, 2, Duration.ofMinutes(2), 1000, java.util.List.of(4L));

        var daily = stats.getDailyStatsForDeck(deckId);
        assertFalse(daily.isEmpty());
        int percent = stats.getDeckProgressPercent(deckId, 10);
        assertTrue(percent > 0);
        assertEquals(4, stats.getKnownCardIds(deckId).size());
    }
}


