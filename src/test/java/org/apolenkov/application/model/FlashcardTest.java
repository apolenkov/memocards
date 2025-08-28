package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Flashcard Model Tests")
class FlashcardTest {

    private Flashcard flashcard;
    private final LocalDateTime testTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        flashcard = new Flashcard();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize with current time")
        void defaultConstructorShouldInitializeWithCurrentTime() {
            Flashcard newFlashcard = new Flashcard();

            assertThat(newFlashcard.getId()).isNull();
            assertThat(newFlashcard.getDeckId()).isZero();
            assertThat(newFlashcard.getFrontText()).isNull();
            assertThat(newFlashcard.getBackText()).isNull();
            assertThat(newFlashcard.getExample()).isNull();
            assertThat(newFlashcard.getImageUrl()).isNull();
            assertThat(newFlashcard.getCreatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
            assertThat(newFlashcard.getUpdatedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Three-parameter constructor should set basic fields")
        void threeParameterConstructorShouldSetBasicFields() {
            Flashcard newFlashcard = new Flashcard(1L, 2L, "Front", "Back");

            assertThat(newFlashcard.getId()).isEqualTo(1L);
            assertThat(newFlashcard.getDeckId()).isEqualTo(2L);
            assertThat(newFlashcard.getFrontText()).isEqualTo("Front");
            assertThat(newFlashcard.getBackText()).isEqualTo("Back");
            assertThat(newFlashcard.getExample()).isNull();
            assertThat(newFlashcard.getImageUrl()).isNull();
        }

        @Test
        @DisplayName("Four-parameter constructor should set all fields including example")
        void fourParameterConstructorShouldSetAllFieldsIncludingExample() {
            Flashcard newFlashcard = new Flashcard(1L, 2L, "Front", "Back", "Example");

            assertThat(newFlashcard.getId()).isEqualTo(1L);
            assertThat(newFlashcard.getDeckId()).isEqualTo(2L);
            assertThat(newFlashcard.getFrontText()).isEqualTo("Front");
            assertThat(newFlashcard.getBackText()).isEqualTo("Back");
            assertThat(newFlashcard.getExample()).isEqualTo("Example");
            assertThat(newFlashcard.getImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Id getter and setter should work correctly")
        void idGetterAndSetterShouldWorkCorrectly() {
            flashcard.setId(123L);
            assertThat(flashcard.getId()).isEqualTo(123L);
        }

        @Test
        @DisplayName("DeckId getter and setter should work correctly")
        void deckIdGetterAndSetterShouldWorkCorrectly() {
            flashcard.setDeckId(456L);
            assertThat(flashcard.getDeckId()).isEqualTo(456L);
        }

        @Test
        @DisplayName("DeckId setter should throw exception for non-positive values")
        void deckIdSetterShouldThrowExceptionForNonPositiveValues() {
            assertThatThrownBy(() -> flashcard.setDeckId(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("deckId must be positive");

            assertThatThrownBy(() -> flashcard.setDeckId(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("deckId must be positive");
        }

        @Test
        @DisplayName("FrontText getter and setter should work correctly")
        void frontTextGetterAndSetterShouldWorkCorrectly() {
            flashcard.setFrontText("New Front Text");
            assertThat(flashcard.getFrontText()).isEqualTo("New Front Text");
        }

        @Test
        @DisplayName("FrontText setter should trim whitespace")
        void frontTextSetterShouldTrimWhitespace() {
            flashcard.setFrontText("  Trimmed Front Text  ");
            assertThat(flashcard.getFrontText()).isEqualTo("Trimmed Front Text");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("FrontText setter should throw exception for null or empty text")
        void frontTextSetterShouldThrowExceptionForNullOrEmptyText(final String text) {
            assertThatThrownBy(() -> flashcard.setFrontText(text))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("frontText is required");
        }

        @Test
        @DisplayName("BackText getter and setter should work correctly")
        void backTextGetterAndSetterShouldWorkCorrectly() {
            flashcard.setBackText("New Back Text");
            assertThat(flashcard.getBackText()).isEqualTo("New Back Text");
        }

        @Test
        @DisplayName("BackText setter should trim whitespace")
        void backTextSetterShouldTrimWhitespace() {
            flashcard.setBackText("  Trimmed Back Text  ");
            assertThat(flashcard.getBackText()).isEqualTo("Trimmed Back Text");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("BackText setter should throw exception for null or empty text")
        void backTextSetterShouldThrowExceptionForNullOrEmptyText(final String text) {
            assertThatThrownBy(() -> flashcard.setBackText(text))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("backText is required");
        }

        @Test
        @DisplayName("Example getter and setter should work correctly")
        void exampleGetterAndSetterShouldWorkCorrectly() {
            flashcard.setExample("New Example");
            assertThat(flashcard.getExample()).isEqualTo("New Example");
        }

        @Test
        @DisplayName("Example setter should trim whitespace")
        void exampleSetterShouldTrimWhitespace() {
            flashcard.setExample("  Trimmed Example  ");
            assertThat(flashcard.getExample()).isEqualTo("Trimmed Example");
        }

        @Test
        @DisplayName("Example setter should handle null")
        void exampleSetterShouldHandleNull() {
            flashcard.setExample(null);
            assertThat(flashcard.getExample()).isNull();
        }

        @Test
        @DisplayName("ImageUrl getter and setter should work correctly")
        void imageUrlGetterAndSetterShouldWorkCorrectly() {
            flashcard.setImageUrl("https://example.com/image.jpg");
            assertThat(flashcard.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("ImageUrl setter should trim whitespace")
        void imageUrlSetterShouldTrimWhitespace() {
            flashcard.setImageUrl("  https://example.com/image.jpg  ");
            assertThat(flashcard.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        }

        @Test
        @DisplayName("ImageUrl setter should handle null")
        void imageUrlSetterShouldHandleNull() {
            flashcard.setImageUrl(null);
            assertThat(flashcard.getImageUrl()).isNull();
        }

        @Test
        @DisplayName("CreatedAt getter and setter should work correctly")
        void createdAtGetterAndSetterShouldWorkCorrectly() {
            flashcard.setCreatedAt(testTime);
            assertThat(flashcard.getCreatedAt()).isEqualTo(testTime);
        }

        @Test
        @DisplayName("UpdatedAt getter and setter should work correctly")
        void updatedAtGetterAndSetterShouldWorkCorrectly() {
            flashcard.setUpdatedAt(testTime);
            assertThat(flashcard.getUpdatedAt()).isEqualTo(testTime);
        }
    }

    @Nested
    @DisplayName("Timestamp Update Tests")
    class TimestampUpdateTests {

        private void assertTimestampUpdated(final Consumer<Flashcard> mutation) {
            LocalDateTime beforeUpdate = flashcard.getUpdatedAt();

            await().atMost(Duration.ofMillis(100));

            mutation.accept(flashcard);
            assertThat(flashcard.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("FrontText setter should update timestamp")
        void frontTextSetterShouldUpdateTimestamp() {
            assertTimestampUpdated(fc -> fc.setFrontText("New Front Text"));
        }

        @Test
        @DisplayName("BackText setter should update timestamp")
        void backTextSetterShouldUpdateTimestamp() {
            assertTimestampUpdated(fc -> fc.setBackText("New Back Text"));
        }

        @Test
        @DisplayName("Example setter should update timestamp")
        void exampleSetterShouldUpdateTimestamp() {
            assertTimestampUpdated(fc -> fc.setExample("New Example"));
        }

        @Test
        @DisplayName("ImageUrl setter should update timestamp")
        void imageUrlSetterShouldUpdateTimestamp() {
            assertTimestampUpdated(fc -> fc.setImageUrl("https://example.com/image.jpg"));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Equals should work correctly")
        void equalsShouldWorkCorrectly() {
            Flashcard flashcard1 = new Flashcard();
            flashcard1.setId(1L);

            Flashcard flashcard2 = new Flashcard();
            flashcard2.setId(1L);

            Flashcard flashcard3 = new Flashcard();
            flashcard3.setId(2L);

            assertThat(flashcard1)
                    .isEqualTo(flashcard2)
                    .isNotEqualTo(flashcard3)
                    .isNotEqualTo(null);
        }

        @Test
        @DisplayName("HashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            Flashcard flashcard1 = new Flashcard();
            flashcard1.setId(1L);

            Flashcard flashcard2 = new Flashcard();
            flashcard2.setId(1L);

            assertThat(flashcard1).hasSameHashCodeAs(flashcard2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("ToString should contain all relevant information")
        void toStringShouldContainAllRelevantInformation() {
            flashcard.setId(1L);
            flashcard.setDeckId(2L);
            flashcard.setFrontText("Front Text");
            flashcard.setBackText("Back Text");
            flashcard.setExample("Example Text");
            flashcard.setImageUrl("https://example.com/image.jpg");
            flashcard.setCreatedAt(testTime);
            flashcard.setUpdatedAt(testTime);

            String result = flashcard.toString();

            assertThat(result)
                    .contains("id=1")
                    .contains("deckId=2")
                    .contains("frontText='Front Text'")
                    .contains("backText='Back Text'")
                    .contains("example='Example Text'")
                    .contains("imageUrl='https://example.com/image.jpg'")
                    .contains("createdAt=" + testTime)
                    .contains("updatedAt=" + testTime);
        }

        @Test
        @DisplayName("ToString should handle null values correctly")
        void toStringShouldHandleNullValuesCorrectly() {
            flashcard.setId(1L);
            flashcard.setDeckId(2L);
            flashcard.setFrontText("Front");
            flashcard.setBackText("Back");
            // Leave example and imageUrl as null

            String result = flashcard.toString();

            assertThat(result).contains("example='null'").contains("imageUrl='null'");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle very long text within limits")
        void shouldHandleVeryLongTextWithinLimits() {
            String longText = "a".repeat(300);
            flashcard.setFrontText(longText);
            flashcard.setBackText(longText);

            assertThat(flashcard.getFrontText()).isEqualTo(longText);
            assertThat(flashcard.getBackText()).isEqualTo(longText);
        }

        @Test
        @DisplayName("Should handle very long example within limits")
        void shouldHandleVeryLongExampleWithinLimits() {
            String longExample = "a".repeat(500);
            flashcard.setExample(longExample);

            assertThat(flashcard.getExample()).isEqualTo(longExample);
        }

        @Test
        @DisplayName("Should handle very long imageUrl within limits")
        void shouldHandleVeryLongImageUrlWithinLimits() {
            String longUrl = "https://example.com/" + "a".repeat(2000);
            flashcard.setImageUrl(longUrl);

            assertThat(flashcard.getImageUrl()).isEqualTo(longUrl);
        }
    }
}
