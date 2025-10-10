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
        SessionStatsDto stats = SessionStatsDto.of(1L, 10, 8, 1, 60000L, 15000L, List.of(1L, 2L));

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
        SessionStatsDto.of(1L, 0, 0, 0, 0L, 0L, Set.of());
    }

    @Test
    @DisplayName("Should validate deck id")
    void shouldValidateDeckId() {
        assertThatThrownBy(this::createInvalidSessionStatsWithZeroDeckId).isInstanceOf(IllegalArgumentException.class);
    }

    private void createInvalidSessionStatsWithZeroDeckId() {
        SessionStatsDto.of(0L, 10, 8, 0, 0L, 0L, null);
    }

    @Test
    @DisplayName("Should handle null known card ids delta")
    void shouldHandleNullKnownCardIdsDelta() {
        SessionStatsDto stats = SessionStatsDto.of(1L, 10, 8, 0, 60000L, 15000L, null);

        assertThat(stats.knownCardIdsDelta()).isNull();
    }
}
