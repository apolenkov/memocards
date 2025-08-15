package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
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
            assertThat(newFlashcard.getDeckId()).isNull();
            assertThat(newFlashcard.getFrontText()).isNull();
            assertThat(newFlashcard.getBackText()).isNull();
            assertThat(newFlashcard.getExample()).isNull();
            assertThat(newFlashcard.getImageUrl()).isNull();
            assertThat(newFlashcard.getCreatedAt())
                    .isCloseTo(LocalDateTime.now(), within(1, java.time.temporal.ChronoUnit.SECONDS));
            assertThat(newFlashcard.getUpdatedAt())
                    .isCloseTo(LocalDateTime.now(), within(1, java.time.temporal.ChronoUnit.SECONDS));
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
        @DisplayName("DeckId setter should throw exception for null")
        void deckIdSetterShouldThrowExceptionForNull() {
            assertThatThrownBy(() -> flashcard.setDeckId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("deckId is required");
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
        void frontTextSetterShouldThrowExceptionForNullOrEmptyText(String text) {
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
        void backTextSetterShouldThrowExceptionForNullOrEmptyText(String text) {
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

        @Test
        @DisplayName("FrontText setter should update timestamp")
        void frontTextSetterShouldUpdateTimestamp() {
            LocalDateTime beforeUpdate = flashcard.getUpdatedAt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }

            flashcard.setFrontText("New Front Text");

            assertThat(flashcard.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("BackText setter should update timestamp")
        void backTextSetterShouldUpdateTimestamp() {
            LocalDateTime beforeUpdate = flashcard.getUpdatedAt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }

            flashcard.setBackText("New Back Text");

            assertThat(flashcard.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Example setter should update timestamp")
        void exampleSetterShouldUpdateTimestamp() {
            LocalDateTime beforeUpdate = flashcard.getUpdatedAt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }

            flashcard.setExample("New Example");

            assertThat(flashcard.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("ImageUrl setter should update timestamp")
        void imageUrlSetterShouldUpdateTimestamp() {
            LocalDateTime beforeUpdate = flashcard.getUpdatedAt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }

            flashcard.setImageUrl("https://example.com/image.jpg");

            assertThat(flashcard.getUpdatedAt()).isAfter(beforeUpdate);
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

            assertThat(flashcard1).isEqualTo(flashcard2);
            assertThat(flashcard1).isNotEqualTo(flashcard3);
            assertThat(flashcard1).isNotEqualTo(null);
            assertThat(flashcard1).isNotEqualTo("string");
        }

        @Test
        @DisplayName("HashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            Flashcard flashcard1 = new Flashcard();
            flashcard1.setId(1L);

            Flashcard flashcard2 = new Flashcard();
            flashcard2.setId(1L);

            assertThat(flashcard1.hashCode()).isEqualTo(flashcard2.hashCode());
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
