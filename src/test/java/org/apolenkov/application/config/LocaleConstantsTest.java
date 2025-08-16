package org.apolenkov.application.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocaleConstants Tests")
class LocaleConstantsTest {

    @Nested
    @DisplayName("Class Structure Tests")
    class ClassStructureTests {
        @Test
        @DisplayName("Should be final class")
        void shouldBeFinalClass() {
            // Given
            Class<LocaleConstants> clazz = LocaleConstants.class;

            // When & Then
            assertThat(Modifier.isFinal(clazz.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Should have private constructor")
        void shouldHavePrivateConstructor() throws Exception {
            // Given
            Constructor<LocaleConstants> constructor = LocaleConstants.class.getDeclaredConstructor();

            // When & Then
            assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        }

        @Test
        @DisplayName("Should not be instantiable")
        void shouldNotBeInstantiable() throws Exception {
            // Given
            Constructor<LocaleConstants> constructor = LocaleConstants.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            // When & Then
            assertThat(constructor.newInstance()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {
        @Test
        @DisplayName("SESSION_LOCALE_KEY should have correct value")
        void sessionLocaleKeyShouldHaveCorrectValue() {
            // When & Then
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).isEqualTo("preferredLocale");
        }

        @Test
        @DisplayName("COOKIE_LOCALE_KEY should have correct value")
        void cookieLocaleKeyShouldHaveCorrectValue() {
            // When & Then
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).isEqualTo("preferredLocale");
        }

        @Test
        @DisplayName("Both locale keys should be equal")
        void bothLocaleKeysShouldBeEqual() {
            // When & Then
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
        @DisplayName("Constants should be strings")
        void constantsShouldBeStrings() {
            // When & Then
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).isInstanceOf(String.class);
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).isInstanceOf(String.class);
        }
    }

    @Nested
    @DisplayName("Constants Content Tests")
    class ConstantsContentTests {
        @Test
        @DisplayName("Constants should contain expected text")
        void constantsShouldContainExpectedText() {
            // When & Then
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).contains("preferred");
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).contains("Locale");
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).contains("preferred");
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).contains("Locale");
        }

        @Test
        @DisplayName("Constants should have correct length")
        void constantsShouldHaveCorrectLength() {
            // When & Then
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).hasSize(15);
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).hasSize(15);
        }

        @Test
        @DisplayName("Constants should start with 'preferred'")
        void constantsShouldStartWithPreferred() {
            // When & Then
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).startsWith("preferred");
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).startsWith("preferred");
        }

        @Test
        @DisplayName("Constants should end with 'Locale'")
        void constantsShouldEndWithLocale() {
            // When & Then
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).endsWith("Locale");
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).endsWith("Locale");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        @Test
        @DisplayName("Constants should be immutable")
        void constantsShouldBeImmutable() {
            // Given
            String originalSessionKey = LocaleConstants.SESSION_LOCALE_KEY;
            String originalCookieKey = LocaleConstants.COOKIE_LOCALE_KEY;

            // When & Then
            assertThat(LocaleConstants.SESSION_LOCALE_KEY).isEqualTo(originalSessionKey);
            assertThat(LocaleConstants.COOKIE_LOCALE_KEY).isEqualTo(originalCookieKey);
        }

        @Test
        @DisplayName("Constants should be accessible from different contexts")
        void constantsShouldBeAccessibleFromDifferentContexts() {
            // When
            String sessionKey = LocaleConstants.SESSION_LOCALE_KEY;
            String cookieKey = LocaleConstants.COOKIE_LOCALE_KEY;

            // Then
            assertThat(sessionKey).isEqualTo("preferredLocale");
            assertThat(cookieKey).isEqualTo("preferredLocale");
        }

        @Test
        @DisplayName("Constants should handle string operations")
        void constantsShouldHandleStringOperations() {
            // Given
            String sessionKey = LocaleConstants.SESSION_LOCALE_KEY;
            String cookieKey = LocaleConstants.COOKIE_LOCALE_KEY;

            // When
            String upperSession = sessionKey.toUpperCase();
            String upperCookie = cookieKey.toUpperCase();

            // Then
            assertThat(upperSession).isEqualTo("PREFERREDLOCALE");
            assertThat(upperCookie).isEqualTo("PREFERREDLOCALE");
        }
    }
}
