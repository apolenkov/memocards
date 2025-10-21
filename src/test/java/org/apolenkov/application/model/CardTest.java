package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Card Domain Model Tests")
class CardTest {

    private Card testCard;

    @BeforeEach
    void setUp() {
        testCard = new Card(1L, 1L, "Test Front", "Test Back", "Test Example");
    }

    @Test
    @DisplayName("Should create card with valid parameters")
    void shouldCreateCardWithValidParameters() {
        Card card = new Card(1L, 1L, "Front", "Back");

        assertThat(card).isNotNull();
        assertThat(card.getId()).isEqualTo(1L);
        assertThat(card.getDeckId()).isEqualTo(1L);
        assertThat(card.getFrontText()).isEqualTo("Front");
        assertThat(card.getBackText()).isEqualTo("Back");
        assertThat(card.getExample()).isNull();
        assertThat(card.getCreatedAt()).isNotNull();
        assertThat(card.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should create card with example")
    void shouldCreateCardWithExample() {
        Card card = new Card(1L, 1L, "Front", "Back", "Example");

        assertThat(card.getExample()).isEqualTo("Example");
    }

    @Test
    @DisplayName("Should throw exception for invalid deckId")
    void shouldThrowExceptionForInvalidDeckId() {
        assertThatThrownBy(() -> new Card(1L, 0L, "Front", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deckId must be positive");

        assertThatThrownBy(() -> new Card(1L, -1L, "Front", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("deckId must be positive");
    }

    @Test
    @DisplayName("Should throw exception for null front text")
    void shouldThrowExceptionForNullFrontText() {
        assertThatThrownBy(() -> new Card(1L, 1L, null, "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frontText is required");
    }

    @Test
    @DisplayName("Should throw exception for empty front text")
    void shouldThrowExceptionForEmptyFrontText() {
        assertThatThrownBy(() -> new Card(1L, 1L, "", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frontText is required");

        assertThatThrownBy(() -> new Card(1L, 1L, "   ", "Back"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("frontText is required");
    }

    @Test
    @DisplayName("Should throw exception for null back text")
    void shouldThrowExceptionForNullBackText() {
        assertThatThrownBy(() -> new Card(1L, 1L, "Front", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backText is required");
    }

    @Test
    @DisplayName("Should throw exception for empty back text")
    void shouldThrowExceptionForEmptyBackText() {
        assertThatThrownBy(() -> new Card(1L, 1L, "Front", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backText is required");

        assertThatThrownBy(() -> new Card(1L, 1L, "Front", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("backText is required");
    }

    @Test
    @DisplayName("Should handle null example")
    void shouldHandleNullExample() {
        Card card = new Card(1L, 1L, "Front", "Back", null);

        assertThat(card.getExample()).isNull();
    }

    @Test
    @DisplayName("Should handle empty example")
    void shouldHandleEmptyExample() {
        Card card = new Card(1L, 1L, "Front", "Back", "");

        assertThat(card.getExample()).isEmpty();
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        // Given
        long newId = 999L;

        // When
        testCard.setId(newId);

        // Then
        assertThat(testCard.getId()).isEqualTo(newId);
    }

    @Test
    @DisplayName("Should set and get deckId")
    void shouldSetAndGetDeckId() {
        long newDeckId = 999L;
        testCard.setDeckId(newDeckId);

        assertThat(testCard.getDeckId()).isEqualTo(newDeckId);
    }

    @Test
    @DisplayName("Should set and get front text")
    void shouldSetAndGetFrontText() {
        String newFrontText = "New Front Text";
        testCard.setFrontText(newFrontText);

        assertThat(testCard.getFrontText()).isEqualTo(newFrontText);
        assertThat(testCard.getUpdatedAt()).isAfter(testCard.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get back text")
    void shouldSetAndGetBackText() {
        String newBackText = "New Back Text";
        testCard.setBackText(newBackText);

        assertThat(testCard.getBackText()).isEqualTo(newBackText);
        assertThat(testCard.getUpdatedAt()).isAfter(testCard.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get example")
    void shouldSetAndGetExample() {
        String newExample = "New Example";
        testCard.setExample(newExample);

        assertThat(testCard.getExample()).isEqualTo(newExample);
        assertThat(testCard.getUpdatedAt()).isAfter(testCard.getCreatedAt());
    }

    @Test
    @DisplayName("Should set and get image URL")
    void shouldSetAndGetImageUrl() {
        String imageUrl = "https://example.com/image.jpg";
        testCard.setImageUrl(imageUrl);

        assertThat(testCard.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("Should handle null image URL")
    void shouldHandleNullImageUrl() {
        testCard.setImageUrl(null);

        assertThat(testCard.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("Should validate front text length constraint")
    void shouldValidateFrontTextLengthConstraint() {
        // @Size(max = 300) is JPA validation, not runtime validation
        String longFrontText = "A".repeat(301);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testCard.setFrontText(longFrontText);
        assertThat(testCard.getFrontText()).isEqualTo(longFrontText);
    }

    @Test
    @DisplayName("Should validate back text length constraint")
    void shouldValidateBackTextLengthConstraint() {
        // @Size(max = 300) is JPA validation, not runtime validation
        String longBackText = "A".repeat(301);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testCard.setBackText(longBackText);
        assertThat(testCard.getBackText()).isEqualTo(longBackText);
    }

    @Test
    @DisplayName("Should validate example length constraint")
    void shouldValidateExampleLengthConstraint() {
        // @Size(max = 500) is JPA validation, not runtime validation
        String longExample = "A".repeat(501);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testCard.setExample(longExample);
        assertThat(testCard.getExample()).isEqualTo(longExample);
    }

    @Test
    @DisplayName("Should validate image URL length constraint")
    void shouldValidateImageUrlLengthConstraint() {
        // @Size(max = 2048) is JPA validation, not runtime validation
        String longImageUrl = "A".repeat(2049);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testCard.setImageUrl(longImageUrl);
        assertThat(testCard.getImageUrl()).isEqualTo(longImageUrl);
    }

    @Test
    @DisplayName("Should handle special characters in text")
    void shouldHandleSpecialCharactersInText() {
        String specialText = "Text with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        testCard.setFrontText(specialText);

        assertThat(testCard.getFrontText()).isEqualTo(specialText);
    }

    @Test
    @DisplayName("Should handle unicode characters in text")
    void shouldHandleUnicodeCharactersInText() {
        String unicodeText = "Text with unicode characters: 你好世界";
        testCard.setFrontText(unicodeText);

        assertThat(testCard.getFrontText()).isEqualTo(unicodeText);
    }

    @Test
    @DisplayName("Should handle very long valid text")
    void shouldHandleVeryLongValidText() {
        String longText = "A".repeat(300); // Max allowed size
        testCard.setFrontText(longText);

        assertThat(testCard.getFrontText()).isEqualTo(longText);
    }

    @Test
    @DisplayName("Should handle very long valid example")
    void shouldHandleVeryLongValidExample() {
        String longExample = "A".repeat(500); // Max allowed size
        testCard.setExample(longExample);

        assertThat(testCard.getExample()).isEqualTo(longExample);
    }

    @Test
    @DisplayName("Should handle very long valid image URL")
    void shouldHandleVeryLongValidImageUrl() {
        String longUrl = "https://example.com/" + "A".repeat(2048); // Max allowed size
        testCard.setImageUrl(longUrl);

        assertThat(testCard.getImageUrl()).isEqualTo(longUrl);
    }

    @Test
    @DisplayName("Should update timestamps when modifying content")
    void shouldUpdateTimestampsWhenModifyingContent() {
        LocalDateTime originalUpdatedAt = testCard.getUpdatedAt();

        // Modify content to trigger timestamp update
        testCard.setFrontText("Updated Front");
        testCard.setBackText("Updated Back");

        // Verify that timestamps were updated
        assertThat(testCard.getUpdatedAt()).isAfter(originalUpdatedAt);
        assertThat(testCard.getCreatedAt()).isBefore(testCard.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle card with all fields set")
    void shouldHandleCardWithAllFieldsSet() {
        Card card = new Card();
        card.setId(1L);
        card.setDeckId(1L);
        card.setFrontText("Front");
        card.setBackText("Back");
        card.setExample("Example");
        card.setImageUrl("https://example.com/image.jpg");

        assertThat(card.getId()).isEqualTo(1L);
        assertThat(card.getDeckId()).isEqualTo(1L);
        assertThat(card.getFrontText()).isEqualTo("Front");
        assertThat(card.getBackText()).isEqualTo("Back");
        assertThat(card.getExample()).isEqualTo("Example");
        assertThat(card.getImageUrl()).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should handle card with minimal fields")
    void shouldHandleCardWithMinimalFields() {
        Card card = new Card();
        card.setDeckId(1L);
        card.setFrontText("Front");
        card.setBackText("Back");

        assertThat(card.getId()).isNull();
        assertThat(card.getDeckId()).isEqualTo(1L);
        assertThat(card.getFrontText()).isEqualTo("Front");
        assertThat(card.getBackText()).isEqualTo("Back");
        assertThat(card.getExample()).isNull();
        assertThat(card.getImageUrl()).isNull();
    }
}
