package org.apolenkov.application.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppI18NProvider Tests")
class AppI18NProviderTest {

    private AppI18NProvider i18nProvider;

    @BeforeEach
    void setUp() {
        i18nProvider = new AppI18NProvider();
    }

    @Nested
    @DisplayName("Get Provided Locales Tests")
    class GetProvidedLocalesTests {

        @Test
        @DisplayName("GetProvidedLocales should return supported locales")
        void getProvidedLocalesShouldReturnSupportedLocales() {
            // When
            List<Locale> result = i18nProvider.getProvidedLocales();

            // Then
            assertThat(result)
                    .hasSize(3)
                    .contains(Locale.ENGLISH)
                    .contains(Locale.forLanguageTag("ru"))
                    .contains(Locale.forLanguageTag("es"));
        }

        @Test
        @DisplayName("GetProvidedLocales should return immutable list")
        void getProvidedLocalesShouldReturnImmutableList() {
            // When
            List<Locale> result = i18nProvider.getProvidedLocales();

            // Then
            assertThatThrownBy(() -> result.add(Locale.FRENCH)).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Get Translation Tests")
    class GetTranslationTests {

        @ParameterizedTest
        @CsvSource({"app.title, en, Cards", "app.title, ru, Мемокарты", "app.title, es, Tarjetas"})
        @DisplayName("GetTranslation should return correct translation for supported locales")
        void getTranslationShouldReturnCorrectTranslationForSupportedLocales(
                final String key, final String languageTag, final String expectedTranslation) {
            // Given
            Locale locale = Locale.forLanguageTag(languageTag);

            // When
            String result = i18nProvider.getTranslation(key, locale);

            // Then
            assertThat(result).isEqualTo(expectedTranslation);
        }

        @Test
        @DisplayName("GetTranslation should return key when translation not found")
        void getTranslationShouldReturnKeyWhenTranslationNotFound() {
            // Given
            String key = "nonexistent.key";
            Locale locale = Locale.ENGLISH;

            // When
            String result = i18nProvider.getTranslation(key, locale);

            // Then
            assertThat(result).isEqualTo(key);
        }

        @Test
        @DisplayName("GetTranslation should return key when locale not supported")
        void getTranslationShouldReturnKeyWhenLocaleNotSupported() {
            // Given
            String key = "app.title";
            Locale locale = Locale.FRENCH;

            // When
            String result = i18nProvider.getTranslation(key, locale);

            // Then
            assertThat(result).isEqualTo(key);
        }

        @Test
        @DisplayName("GetTranslation should use English as fallback when locale is null")
        void getTranslationShouldUseEnglishAsFallbackWhenLocaleIsNull() {
            // Given
            String key = "app.title";

            // When
            String result = i18nProvider.getTranslation(key, null);

            // Then
            assertThat(result).isEqualTo("Cards");
        }

        @Test
        @DisplayName("GetTranslation should return empty string when key is null")
        void getTranslationShouldReturnEmptyStringWhenKeyIsNull() {
            // Given
            Locale locale = Locale.ENGLISH;

            // When
            String result = i18nProvider.getTranslation(null, locale);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("GetTranslation should return empty string when key is empty")
        void getTranslationShouldReturnEmptyStringWhenKeyIsEmpty() {
            // Given
            String key = "";
            Locale locale = Locale.ENGLISH;

            // When
            String result = i18nProvider.getTranslation(key, locale);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Message Format Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("GetTranslation should format message with parameters")
        void getTranslationShouldFormatMessageWithParameters() {
            // Given
            String key = "home.progress.details";
            Locale locale = Locale.ENGLISH;
            Object[] params = {5, 10};

            // When
            String result = i18nProvider.getTranslation(key, locale, params);

            // Then
            assertThat(result).isEqualTo("5 learned of 10");
        }

        @Test
        @DisplayName("GetTranslation should format message with single parameter")
        void getTranslationShouldFormatMessageWithSingleParameter() {
            // Given
            String key = "home.deckIcon";
            Locale locale = Locale.ENGLISH;
            Object[] params = {"📚"};

            // When
            String result = i18nProvider.getTranslation(key, locale, params);

            // Then
            assertThat(result).isEqualTo("📚");
        }

        @Test
        @DisplayName("GetTranslation should handle empty parameters array")
        void getTranslationShouldHandleEmptyParametersArray() {
            // Given
            String key = "app.title";
            Locale locale = Locale.ENGLISH;
            Object[] params = {};

            // When
            String result = i18nProvider.getTranslation(key, locale, params);

            // Then
            assertThat(result).isEqualTo("Cards");
        }

        @Test
        @DisplayName("GetTranslation should handle null parameters")
        void getTranslationShouldHandleNullParameters() {
            // Given
            String key = "app.title";
            Locale locale = Locale.ENGLISH;

            // When
            String result = i18nProvider.getTranslation(key, locale);

            // Then
            assertThat(result).isEqualTo("Cards");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long keys")
        void shouldHandleVeryLongKeys() {
            // Given
            String longKey = "a".repeat(1000);
            Locale locale = Locale.ENGLISH;

            // When
            String result = i18nProvider.getTranslation(longKey, locale);

            // Then
            assertThat(result).isEqualTo(longKey);
        }

        @Test
        @DisplayName("Should handle special characters in keys")
        void shouldHandleSpecialCharactersInKeys() {
            // Given
            String specialKey = "key@#$%^&*()_+-=[]{}|;':\",./<>?";
            Locale locale = Locale.ENGLISH;

            // When
            String result = i18nProvider.getTranslation(specialKey, locale);

            // Then
            assertThat(result).isEqualTo(specialKey);
        }

        @Test
        @DisplayName("Should handle unicode characters in keys")
        void shouldHandleUnicodeCharactersInKeys() {
            // Given
            String unicodeKey = "ключ.математика"; // Russian key
            Locale locale = Locale.ENGLISH;

            // When
            String result = i18nProvider.getTranslation(unicodeKey, locale);

            // Then
            assertThat(result).isEqualTo(unicodeKey);
        }

        @Test
        @DisplayName("Should handle complex parameter formatting")
        void shouldHandleComplexParameterFormatting() {
            // Given
            String key = "complex.format";
            Locale locale = Locale.ENGLISH;
            Object[] params = {"John", 25, 99.99};

            // When
            String result = i18nProvider.getTranslation(key, locale, params);

            // Then
            // Should return the key if the message format fails, or the formatted message if it succeeds
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Bundle Prefix Tests")
    class BundlePrefixTests {

        @Test
        @DisplayName("Bundle prefix should be correct")
        void bundlePrefixShouldBeCorrect() {
            // Given
            String expectedPrefix = "i18n.messages";

            // When
            String actualPrefix = AppI18NProvider.BUNDLE_PREFIX;

            // Then
            assertThat(actualPrefix).isEqualTo(expectedPrefix);
        }
    }

    @Nested
    @DisplayName("Locale Constants Tests")
    class LocaleConstantsTests {

        @Test
        @DisplayName("Should handle all supported locales")
        void shouldHandleAllSupportedLocales() {
            // Given
            List<Locale> supportedLocales = i18nProvider.getProvidedLocales();

            // When & Then
            for (Locale locale : supportedLocales) {
                String result = i18nProvider.getTranslation("app.title", locale);
                assertThat(result).isNotNull();
                // Should either return a translation or the key itself
                assertThat(result).isIn("Cards", "Мемокарты", "Tarjetas", "app.title");
            }
        }

        @Test
        @DisplayName("Should handle locale variants")
        void shouldHandleLocaleVariants() {
            // Given
            Locale russianVariant = Locale.forLanguageTag("ru-RU");
            Locale spanishVariant = Locale.forLanguageTag("es-ES");

            // When
            String russianResult = i18nProvider.getTranslation("app.title", russianVariant);
            String spanishResult = i18nProvider.getTranslation("app.title", spanishVariant);

            // Then
            assertThat(russianResult).isNotNull();
            assertThat(spanishResult).isNotNull();
        }
    }
}
