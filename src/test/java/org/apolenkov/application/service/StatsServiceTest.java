package org.apolenkov.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.apolenkov.application.domain.dto.SessionStatsDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SessionStatsDto Core Tests")
class StatsServiceTest {

    @Test
    @DisplayName("Should create valid session stats")
    void shouldCreateValidSessionStats() {
        SessionStatsDto stats = SessionStatsDto.builder()
                .deckId(1L)
                .viewed(10)
                .correct(8)
                .hard(1)
                .sessionDurationMs(60000L)
                .totalAnswerDelayMs(15000L)
                .build();

        assertThat(stats.deckId()).isEqualTo(1L);
        assertThat(stats.viewed()).isEqualTo(10);
        assertThat(stats.correct()).isEqualTo(8);
    }

    @Test
    @DisplayName("Should handle zero values")
    void shouldHandleZeroValues() {
        assertThatThrownBy(this::createInvalidSessionStatsWithZeroValues)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Viewed count must be positive");
    }

    private void createInvalidSessionStatsWithZeroValues() {
        SessionStatsDto.builder()
                .deckId(1L)
                .viewed(0)
                .correct(0)
                .hard(0)
                .sessionDurationMs(0L)
                .totalAnswerDelayMs(0L)
                .knownCardIdsDelta(Set.of())
                .build();
    }

    @Test
    @DisplayName("Should validate deck id")
    void shouldValidateDeckId() {
        assertThatThrownBy(this::createInvalidSessionStatsWithZeroDeckId).isInstanceOf(IllegalArgumentException.class);
    }

    private void createInvalidSessionStatsWithZeroDeckId() {
        SessionStatsDto.builder().deckId(0L).viewed(10).correct(8).build();
    }
}
