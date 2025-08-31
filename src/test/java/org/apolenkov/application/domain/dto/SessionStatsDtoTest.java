package org.apolenkov.application.domain.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SessionStatsDto Core Tests")
class SessionStatsDtoTest {

    @Test
    @DisplayName("Should create valid session stats")
    void shouldCreateValidSessionStats() {
        SessionStatsDto stats = SessionStatsDto.builder()
                .deckId(1L)
                .viewed(10)
                .correct(8)
                .repeat(2)
                .hard(1)
                .sessionDurationMs(60000L)
                .totalAnswerDelayMs(15000L)
                .knownCardIdsDelta(List.of(1L, 2L))
                .build();

        assertThat(stats.deckId()).isEqualTo(1L);
        assertThat(stats.viewed()).isEqualTo(10);
        assertThat(stats.correct()).isEqualTo(8);
        assertThat(stats.knownCardIdsDelta()).hasSize(2).contains(1L, 2L);
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
                .repeat(0)
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

    @Test
    @DisplayName("Should handle null known card ids delta")
    void shouldHandleNullKnownCardIdsDelta() {
        SessionStatsDto stats = SessionStatsDto.builder()
                .deckId(1L)
                .viewed(10)
                .correct(8)
                .repeat(0)
                .hard(0)
                .sessionDurationMs(60000L)
                .totalAnswerDelayMs(15000L)
                .knownCardIdsDelta(null)
                .build();

        assertThat(stats.knownCardIdsDelta()).isNull();
    }
}
