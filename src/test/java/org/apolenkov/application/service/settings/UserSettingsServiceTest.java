package org.apolenkov.application.service.settings;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import org.apolenkov.application.domain.port.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for UserSettingsService.
 * Tests user settings management and validation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserSettingsService Tests")
class UserSettingsServiceTest {

    @Mock
    private UserSettingsRepository repository;

    private UserSettingsService settingsService;

    @BeforeEach
    void setUp() {
        settingsService = new UserSettingsService(repository);
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should throw exception when repository is null")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new UserSettingsService(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("UserSettingsRepository cannot be null");
    }

    // ==================== SetPreferredLocale Tests ====================

    @Test
    @DisplayName("Should save preferred locale")
    void shouldSavePreferredLocale() {
        // Given
        long userId = 1L;
        Locale locale = Locale.ENGLISH;

        // When
        settingsService.setPreferredLocale(userId, locale);

        // Then
        verify(repository).savePreferredLocaleCode(userId, "en");
    }

    @Test
    @DisplayName("Should save Russian locale correctly")
    void shouldSaveRussianLocaleCorrectly() {
        // Given
        long userId = 1L;
        Locale locale = Locale.forLanguageTag("ru");

        // When
        settingsService.setPreferredLocale(userId, locale);

        // Then
        verify(repository).savePreferredLocaleCode(userId, "ru");
    }

    @Test
    @DisplayName("Should throw exception when userId is zero")
    void shouldThrowExceptionWhenUserIdIsZero() {
        assertThatThrownBy(() -> settingsService.setPreferredLocale(0L, Locale.ENGLISH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID must be positive");
    }

    @Test
    @DisplayName("Should throw exception when userId is negative")
    void shouldThrowExceptionWhenUserIdIsNegative() {
        assertThatThrownBy(() -> settingsService.setPreferredLocale(-1L, Locale.ENGLISH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID must be positive");
    }

    @Test
    @DisplayName("Should throw exception when locale is null")
    void shouldThrowExceptionWhenLocaleIsNull() {
        assertThatThrownBy(() -> settingsService.setPreferredLocale(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Locale cannot be null");
    }
}
