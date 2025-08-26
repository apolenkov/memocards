package org.apolenkov.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LocaleConstants Tests")
class LocaleConstantsTest {

    @Test
    @DisplayName("Constants should have correct values")
    void constantsShouldHaveCorrectValues() {
        // When & Then
        assertThat(LocaleConstants.SESSION_LOCALE_KEY).isEqualTo("preferredLocale");
        assertThat(LocaleConstants.COOKIE_LOCALE_KEY).isEqualTo("preferredLocale");
        assertThat(LocaleConstants.SESSION_LOCALE_KEY).isEqualTo(LocaleConstants.COOKIE_LOCALE_KEY);
    }

    @Test
    @DisplayName("Constants should not be null")
    void constantsShouldNotBeNull() {
        // When & Then
        assertThat(LocaleConstants.SESSION_LOCALE_KEY).isNotNull();
        assertThat(LocaleConstants.COOKIE_LOCALE_KEY).isNotNull();
    }

    @Test
    @DisplayName("Class should be final")
    void classShouldBeFinal() {
        // When & Then
        assertThat(Modifier.isFinal(LocaleConstants.class.getModifiers())).isTrue();
    }
}
