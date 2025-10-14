package org.apolenkov.application.views.auth.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for PasswordValidator utility class.
 * Tests password security validation logic.
 */
@DisplayName("PasswordValidator Tests")
class PasswordValidatorTest {

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should throw exception when trying to instantiate")
    void shouldThrowExceptionWhenInstantiating() throws Exception {
        var constructor = PasswordValidator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThatThrownBy(constructor::newInstance)
                .isInstanceOf(ReflectiveOperationException.class)
                .hasCauseInstanceOf(UnsupportedOperationException.class);
    }

    // ==================== IsInvalid Tests ====================

    @Test
    @DisplayName("Should accept valid password with letters and digits")
    void shouldAcceptValidPasswordWithLettersAndDigits() {
        assertThat(PasswordValidator.isInvalid("password123")).isFalse();
    }

    @Test
    @DisplayName("Should accept password with mixed case")
    void shouldAcceptPasswordWithMixedCase() {
        assertThat(PasswordValidator.isInvalid("PassWord123")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"password1", "12345678a", "abcd1234", "Test1234", "MyPass99"})
    @DisplayName("Should accept various valid password formats")
    void shouldAcceptVariousValidPasswordFormats(final String password) {
        assertThat(PasswordValidator.isInvalid(password)).isFalse();
    }

    @Test
    @DisplayName("Should reject null password")
    void shouldRejectNullPassword() {
        assertThat(PasswordValidator.isInvalid(null)).isTrue();
    }

    @Test
    @DisplayName("Should reject password shorter than 8 characters")
    void shouldRejectShortPassword() {
        assertThat(PasswordValidator.isInvalid("pass1")).isTrue();
    }

    @Test
    @DisplayName("Should reject password with only letters")
    void shouldRejectPasswordWithOnlyLetters() {
        assertThat(PasswordValidator.isInvalid("passwordonly")).isTrue();
    }

    @Test
    @DisplayName("Should reject password with only digits")
    void shouldRejectPasswordWithOnlyDigits() {
        assertThat(PasswordValidator.isInvalid("12345678")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "abc", "12345", "1234567", "letters", "LETTERS"})
    @DisplayName("Should reject various invalid password formats")
    void shouldRejectVariousInvalidPasswordFormats(final String password) {
        assertThat(PasswordValidator.isInvalid(password)).isTrue();
    }

    // ==================== ValidateOrThrow Tests ====================

    @Test
    @DisplayName("Should not throw exception for valid password")
    void shouldNotThrowExceptionForValidPassword() {
        assertThatNoException().isThrownBy(() -> PasswordValidator.validateOrThrow("password123"));
    }

    @Test
    @DisplayName("Should throw exception for null password")
    void shouldThrowExceptionForNullPassword() {
        assertThatThrownBy(() -> PasswordValidator.validateOrThrow(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters and contain letters and digits");
    }

    @Test
    @DisplayName("Should throw exception for short password")
    void shouldThrowExceptionForShortPassword() {
        assertThatThrownBy(() -> PasswordValidator.validateOrThrow("pass1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters and contain letters and digits");
    }

    @Test
    @DisplayName("Should throw exception for password without digits")
    void shouldThrowExceptionForPasswordWithoutDigits() {
        assertThatThrownBy(() -> PasswordValidator.validateOrThrow("passwordonly"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters and contain letters and digits");
    }

    @Test
    @DisplayName("Should throw exception for password without letters")
    void shouldThrowExceptionForPasswordWithoutLetters() {
        assertThatThrownBy(() -> PasswordValidator.validateOrThrow("12345678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 8 characters and contain letters and digits");
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should accept exactly 8 character password")
    void shouldAcceptExactlyEightCharacterPassword() {
        assertThat(PasswordValidator.isInvalid("pass1234")).isFalse();
    }

    @Test
    @DisplayName("Should reject exactly 7 character password")
    void shouldRejectSevenCharacterPassword() {
        assertThat(PasswordValidator.isInvalid("pass123")).isTrue();
    }

    @Test
    @DisplayName("Should accept very long password")
    void shouldAcceptVeryLongPassword() {
        String longPassword = "a".repeat(100) + "1";
        assertThat(PasswordValidator.isInvalid(longPassword)).isFalse();
    }
}
