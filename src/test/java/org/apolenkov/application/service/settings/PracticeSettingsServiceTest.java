package org.apolenkov.application.service.settings;

import static org.assertj.core.api.Assertions.assertThat;

import org.apolenkov.application.model.PracticeDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PracticeSettingsService.
 * Tests default practice settings management.
 */
@DisplayName("PracticeSettingsService Tests")
class PracticeSettingsServiceTest {

    private PracticeSettingsService settingsService;

    @BeforeEach
    void setUp() {
        settingsService = new PracticeSettingsService();
    }

    // ==================== Default Count Tests ====================

    @Test
    @DisplayName("Should return default count")
    void shouldReturnDefaultCount() {
        // When
        int result = settingsService.getDefaultCount();

        // Then
        assertThat(result).isEqualTo(10);
    }

    @Test
    @DisplayName("Should update default count")
    void shouldUpdateDefaultCount() {
        // When
        settingsService.setDefaultCount(20);

        // Then
        assertThat(settingsService.getDefaultCount()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should clamp negative count to 1")
    void shouldClampNegativeCountToOne() {
        // When
        settingsService.setDefaultCount(-5);

        // Then
        assertThat(settingsService.getDefaultCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should clamp zero count to 1")
    void shouldClampZeroCountToOne() {
        // When
        settingsService.setDefaultCount(0);

        // Then
        assertThat(settingsService.getDefaultCount()).isEqualTo(1);
    }

    // ==================== Random Order Tests ====================

    @Test
    @DisplayName("Should return default random order as true")
    void shouldReturnDefaultRandomOrderAsTrue() {
        // When
        boolean result = settingsService.isDefaultRandomOrder();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should update default random order")
    void shouldUpdateDefaultRandomOrder() {
        // When
        settingsService.setDefaultRandomOrder(false);

        // Then
        assertThat(settingsService.isDefaultRandomOrder()).isFalse();
    }

    // ==================== Practice Direction Tests ====================

    @Test
    @DisplayName("Should return default direction as FRONT_TO_BACK")
    void shouldReturnDefaultDirectionAsFrontToBack() {
        // When
        PracticeDirection result = settingsService.getDefaultDirection();

        // Then
        assertThat(result).isEqualTo(PracticeDirection.FRONT_TO_BACK);
    }

    @Test
    @DisplayName("Should update default direction")
    void shouldUpdateDefaultDirection() {
        // When
        settingsService.setDefaultDirection(PracticeDirection.BACK_TO_FRONT);

        // Then
        assertThat(settingsService.getDefaultDirection()).isEqualTo(PracticeDirection.BACK_TO_FRONT);
    }

    @Test
    @DisplayName("Should allow both direction types")
    void shouldAllowBothDirectionTypes() {
        // Test FRONT_TO_BACK
        settingsService.setDefaultDirection(PracticeDirection.FRONT_TO_BACK);
        assertThat(settingsService.getDefaultDirection()).isEqualTo(PracticeDirection.FRONT_TO_BACK);

        // Test BACK_TO_FRONT
        settingsService.setDefaultDirection(PracticeDirection.BACK_TO_FRONT);
        assertThat(settingsService.getDefaultDirection()).isEqualTo(PracticeDirection.BACK_TO_FRONT);
    }
}
