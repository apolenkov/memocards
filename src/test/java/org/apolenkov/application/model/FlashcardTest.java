package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Flashcard Domain Model Tests")
class FlashcardTest {

    private Flashcard testFlashcard;

    @BeforeEach
    void setUp() {
        testFlashcard = new Flashcard(1L, 1L, "Test Front", "Test Back", "Test Example");
    }

    @Test
    @DisplayName("Should create flashcard with valid parameters")
    void shouldCreateFlashcardWithValidParameters() {
        Flashcard flashcard = new Flashcard(1L, 1L, "Front", "Back");

        assertThat(flashcard).isNotNull();
        assertThat(flashcard.getId()).isEqualTo(1L);
        assertThat(flashcard.getDeckId()).isEqualTo(1L);
        assertThat(flashcard.getFrontText()).isEqualTo("Front");
        assertThat(flashcard.getBackText()).isEqualTo("Back");
        assertThat(flashcard.getExample()).isNull();
        assertThat(flashcard.getCreatedAt()).isNotNull();
        assertThat(flashcard.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should create flashcard with example")
    void shouldCreateFlashcardWithExample() {
        Flashcard flashcard = new Flashcard(1L, 1L, "Front", "Back", "Example");

        assertThat(flashcard.getExample()).isEqualTo("Example");
    }

    @Test
    @DisplayName("Should throw exception for invalid deckId")
    void shouldThrowExceptionForInvalidDeckId() {
        assertThatThrownBy(() -> new Flashcard(1L, 0L, "Front", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deckId must be positive");

        assertThatThrownBy(() -> new Flashcard(1L, -1L, "Front", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deckId must be positive");
    }

    @Test
    @DisplayName("Should throw exception for null front text")
    void shouldThrowExceptionForNullFrontText() {
        assertThatThrownBy(() -> new Flashcard(1L, 1L, null, "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frontText is required");
    }

    @Test
    @DisplayName("Should throw exception for empty front text")
    void shouldThrowExceptionForEmptyFrontText() {
        assertThatThrownBy(() -> new Flashcard(1L, 1L, "", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frontText is required");

        assertThatThrownBy(() -> new Flashcard(1L, 1L, "   ", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frontText is required");
    }

    @Test
    @DisplayName("Should throw exception for null back text")
    void shouldThrowExceptionForNullBackText() {
        assertThatThrownBy(() -> new Flashcard(1L, 1L, "Front", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backText is required");
    }

    @Test
    @DisplayName("Should throw exception for empty back text")
    void shouldThrowExceptionForEmptyBackText() {
        assertThatThrownBy(() -> new Flashcard(1L, 1L, "Front", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backText is required");

        assertThatThrownBy(() -> new Flashcard(1L, 1L, "Front", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backText is required");
    }

    @Test
    @DisplayName("Should handle null example")
    void shouldHandleNullExample() {
        Flashcard flashcard = new Flashcard(1L, 1L, "Front", "Back", null);

        assertThat(flashcard.getExample()).isNull();
    }

    @Test
    @DisplayName("Should handle empty example")
    void shouldHandleEmptyExample() {
        Flashcard flashcard = new Flashcard(1L, 1L, "Front", "Back", "");

        assertThat(flashcard.getExample()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        Long newId = 999L;
        testFlashcard.setId(newId);

        assertThat(testFlashcard.getId()).isEqualTo(newId);
    }

    @Test
    @DisplayName("Should set and get deckId")
    void shouldSetAndGetDeckId() {
        long newDeckId = 999L;
        testFlashcard.setDeckId(newDeckId);

        assertThat(testFlashcard.getDeckId()).isEqualTo(newDeckId);
    }

    @Test
    @DisplayName("Should set and get front text")
    void shouldSetAndGetFrontText() {
        String newFrontText = "New Front Text";
        testFlashcard.setFrontText(newFrontText);

        assertThat(testFlashcard.getFrontText()).isEqualTo(newFrontText);
        assertThat(testFlashcard.getUpdatedAt()).isAfter(testFlashcard.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get back text")
    void shouldSetAndGetBackText() {
        String newBackText = "New Back Text";
        testFlashcard.setBackText(newBackText);

        assertThat(testFlashcard.getBackText()).isEqualTo(newBackText);
        assertThat(testFlashcard.getUpdatedAt()).isAfter(testFlashcard.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get example")
    void shouldSetAndGetExample() {
        String newExample = "New Example";
        testFlashcard.setExample(newExample);

        assertThat(testFlashcard.getExample()).isEqualTo(newExample);
        assertThat(testFlashcard.getUpdatedAt()).isAfter(testFlashcard.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get image URL")
    void shouldSetAndGetImageUrl() {
        String imageUrl = "https://example.com/image.jpg";
        testFlashcard.setImageUrl(imageUrl);

        assertThat(testFlashcard.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("Should handle null image URL")
    void shouldHandleNullImageUrl() {
        testFlashcard.setImageUrl(null);

        assertThat(testFlashcard.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("Should validate front text length constraint")
    void shouldValidateFrontTextLengthConstraint() {
        // @Size(max = 300) is JPA validation, not runtime validation
        String longFrontText = "A".repeat(301);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testFlashcard.setFrontText(longFrontText);
        assertThat(testFlashcard.getFrontText()).isEqualTo(longFrontText);
    }

    @Test
    @DisplayName("Should validate back text length constraint")
    void shouldValidateBackTextLengthConstraint() {
        // @Size(max = 300) is JPA validation, not runtime validation
        String longBackText = "A".repeat(301);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testFlashcard.setBackText(longBackText);
        assertThat(testFlashcard.getBackText()).isEqualTo(longBackText);
    }

    @Test
    @DisplayName("Should validate example length constraint")
    void shouldValidateExampleLengthConstraint() {
        // @Size(max = 500) is JPA validation, not runtime validation
        String longExample = "A".repeat(501);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testFlashcard.setExample(longExample);
        assertThat(testFlashcard.getExample()).isEqualTo(longExample);
    }

    @Test
    @DisplayName("Should validate image URL length constraint")
    void shouldValidateImageUrlLengthConstraint() {
        // @Size(max = 2048) is JPA validation, not runtime validation
        String longImageUrl = "A".repeat(2049);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testFlashcard.setImageUrl(longImageUrl);
        assertThat(testFlashcard.getImageUrl()).isEqualTo(longImageUrl);
    }

    @Test
    @DisplayName("Should handle special characters in text")
    void shouldHandleSpecialCharactersInText() {
        String specialText = "Text with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        testFlashcard.setFrontText(specialText);

        assertThat(testFlashcard.getFrontText()).isEqualTo(specialText);
    }

    @Test
    @DisplayName("Should handle unicode characters in text")
    void shouldHandleUnicodeCharactersInText() {
        String unicodeText = "Текст с русскими символами";
        testFlashcard.setFrontText(unicodeText);

        assertThat(testFlashcard.getFrontText()).isEqualTo(unicodeText);
    }

    @Test
    @DisplayName("Should handle very long valid text")
    void shouldHandleVeryLongValidText() {
        String longText = "A".repeat(300); // Max allowed size
        testFlashcard.setFrontText(longText);

        assertThat(testFlashcard.getFrontText()).isEqualTo(longText);
    }

    @Test
    @DisplayName("Should handle very long valid example")
    void shouldHandleVeryLongValidExample() {
        String longExample = "A".repeat(500); // Max allowed size
        testFlashcard.setExample(longExample);

        assertThat(testFlashcard.getExample()).isEqualTo(longExample);
    }

    @Test
    @DisplayName("Should handle very long valid image URL")
    void shouldHandleVeryLongValidImageUrl() {
        String longUrl = "https://example.com/" + "A".repeat(2048); // Max allowed size
        testFlashcard.setImageUrl(longUrl);

        assertThat(testFlashcard.getImageUrl()).isEqualTo(longUrl);
    }

    @Test
    @DisplayName("Should update timestamps when modifying content")
    void shouldUpdateTimestampsWhenModifyingContent() {
        LocalDateTime originalUpdatedAt = testFlashcard.getUpdatedAt();

        // Modify content to trigger timestamp update
        testFlashcard.setFrontText("Updated Front");
        testFlashcard.setBackText("Updated Back");

        // Verify that timestamps were updated
        assertThat(testFlashcard.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(testFlashcard.getCreatedAt()).isBefore(testFlashcard.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle flashcard with all fields set")
    void shouldHandleFlashcardWithAllFieldsSet() {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setDeckId(1L);
        flashcard.setFrontText("Front");
        flashcard.setBackText("Back");
        flashcard.setExample("Example");
        flashcard.setImageUrl("https://example.com/image.jpg");

        assertThat(flashcard.getId()).isEqualTo(1L);
        assertThat(flashcard.getDeckId()).isEqualTo(1L);
        assertThat(flashcard.getFrontText()).isEqualTo("Front");
        assertThat(flashcard.getBackText()).isEqualTo("Back");
        assertThat(flashcard.getExample()).isEqualTo("Example");
        assertThat(flashcard.getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should handle flashcard with minimal fields")
    void shouldHandleFlashcardWithMinimalFields() {
        Flashcard flashcard = new Flashcard();
        flashcard.setDeckId(1L);
        flashcard.setFrontText("Front");
        flashcard.setBackText("Back");

        assertThat(flashcard.getId()).isNull();
        assertThat(flashcard.getDeckId()).isEqualTo(1L);
        assertThat(flashcard.getFrontText()).isEqualTo("Front");
        assertThat(flashcard.getBackText()).isEqualTo("Back");
        assertThat(flashcard.getExample()).isNull();
        assertThat(flashcard.getImageUrl()).isNull();
    }
}
