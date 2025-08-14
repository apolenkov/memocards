package org.apolenkov.application.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.apolenkov.application.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"dev", "memory"})
class StatsIT {

    @Autowired
    StatsService statsService;

    @Test
    void recordSession_and_queryDailyStats() {
        long deckId = 99L;
        statsService.recordSession(deckId, 10, 7, 1, 2, Duration.ofMinutes(5), 12000L, List.of(1L, 2L));
        var daily = statsService.getDailyStatsForDeck(deckId);
        assertThat(daily).isNotEmpty();
        assertThat(daily.get(0).viewed).isGreaterThanOrEqualTo(10);
        Set<Long> known = statsService.getKnownCardIds(deckId);
        assertThat(known).contains(1L, 2L);
        int progress = statsService.getDeckProgressPercent(deckId, 10);
        assertThat(progress).isBetween(0, 100);
    }
}
