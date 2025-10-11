package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Deck Domain Model Tests")
class DeckTest {

    private Deck testDeck;
    private Flashcard testFlashcard;

    @BeforeEach
    void setUp() {
        testDeck = new Deck(1L, 1L, "Test Deck", "Test Description");
        testFlashcard = new Flashcard(1L, 1L, "Front", "Back", "Example");
    }

    @Test
    @DisplayName("Should create deck with valid parameters")
    void shouldCreateDeckWithValidParameters() {
        Deck deck = Deck.create(1L, "Valid Title", "Valid Description");

        assertThat(deck).isNotNull();
        assertThat(deck.getUserId()).isEqualTo(1L);
        assertThat(deck.getTitle()).isEqualTo("Valid Title");
        assertThat(deck.getDescription()).isEqualTo("Valid Description");
        assertThat(deck.getCreatedAt()).isNotNull();
        assertThat(deck.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception for invalid userId")
    void shouldThrowExceptionForInvalidUserId() {
        assertThatThrownBy(() -> Deck.create(0L, "Valid Title", "Valid Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId must be positive");

        assertThatThrownBy(() -> Deck.create(-1L, "Valid Title", "Valid Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userId must be positive");
    }

    @Test
    @DisplayName("Should throw exception for null title")
    void shouldThrowExceptionForNullTitle() {
        assertThatThrownBy(() -> Deck.create(1L, null, "Valid Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title is required");
    }

    @Test
    @DisplayName("Should throw exception for empty title")
    void shouldThrowExceptionForEmptyTitle() {
        assertThatThrownBy(() -> Deck.create(1L, "", "Valid Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title is required");

        assertThatThrownBy(() -> Deck.create(1L, "   ", "Valid Description"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title is required");
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        Deck deck = Deck.create(1L, "Valid Title", null);

        assertThat(deck.getDescription()).isNull();
    }

    @Test
    @DisplayName("Should trim title and description")
    void shouldTrimTitleAndDescription() {
        Deck deck = Deck.create(1L, "  Title  ", "  Description  ");

        assertThat(deck.getTitle()).isEqualTo("Title");
        assertThat(deck.getDescription()).isEqualTo("Description");
    }

    @Test
    @DisplayName("Should add flashcard to deck")
    void shouldAddFlashcardToDeck() {
        testDeck.addFlashcard(testFlashcard);

        assertThat(testDeck.getFlashcards()).hasSize(1);
        assertThat(testDeck.getFlashcardCount()).isEqualTo(1);
        assertThat(testDeck.getFlashcards().getFirst()).isEqualTo(testFlashcard);
    }

    @Test
    @DisplayName("Should throw exception when adding null flashcard")
    void shouldThrowExceptionWhenAddingNullFlashcard() {
        assertThatThrownBy(() -> testDeck.addFlashcard(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("flashcard is null");
    }

    @Test
    @DisplayName("Should remove flashcard from deck")
    void shouldRemoveFlashcardFromDeck() {
        testDeck.addFlashcard(testFlashcard);
        assertThat(testDeck.getFlashcardCount()).isEqualTo(1);

        testDeck.removeFlashcard(testFlashcard);
        assertThat(testDeck.getFlashcardCount()).isZero();
        assertThat(testDeck.getFlashcards()).isEmpty();
    }

    @Test
    @DisplayName("Should handle removing non-existent flashcard")
    void shouldHandleRemovingNonExistentFlashcard() {
        Flashcard nonExistentFlashcard = new Flashcard(999L, 1L, "Question", "Answer");
        testDeck.addFlashcard(testFlashcard);
        int initialCount = testDeck.getFlashcardCount();

        testDeck.removeFlashcard(nonExistentFlashcard);

        assertThat(testDeck.getFlashcardCount()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("Should get flashcard count")
    void shouldGetFlashcardCount() {
        assertThat(testDeck.getFlashcardCount()).isZero();

        testDeck.addFlashcard(testFlashcard);
        assertThat(testDeck.getFlashcardCount()).isEqualTo(1);

        testDeck.addFlashcard(new Flashcard(2L, 1L, "Q2", "A2"));
        assertThat(testDeck.getFlashcardCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should update title")
    void shouldUpdateTitle() {
        String newTitle = "Updated Title";
        LocalDateTime originalUpdatedAt = testDeck.getUpdatedAt();

        testDeck.setTitle(newTitle);

        assertThat(testDeck.getTitle()).isEqualTo(newTitle);
        assertThat(testDeck.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should update description")
    void shouldUpdateDescription() {
        String newDescription = "Updated Description";
        LocalDateTime originalUpdatedAt = testDeck.getUpdatedAt();

        testDeck.setDescription(newDescription);

        assertThat(testDeck.getDescription()).isEqualTo(newDescription);
        assertThat(testDeck.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("Should handle empty deck")
    void shouldHandleEmptyDeck() {
        assertThat(testDeck.getFlashcardCount()).isZero();
        assertThat(testDeck.getFlashcards()).isEmpty();
    }

    @Test
    @DisplayName("Should maintain flashcard order")
    void shouldMaintainFlashcardOrder() {
        Flashcard card1 = new Flashcard(1L, 1L, "Front 1", "Back 1");
        Flashcard card2 = new Flashcard(2L, 1L, "Front 2", "Back 2");

        testDeck.addFlashcard(card1);
        testDeck.addFlashcard(card2);

        List<Flashcard> flashcards = testDeck.getFlashcards();
        assertThat(flashcards).hasSize(2);
        assertThat(flashcards.getFirst()).isEqualTo(card1);
        assertThat(flashcards.get(1)).isEqualTo(card2);
    }

    @Test
    @DisplayName("Should handle deck with multiple flashcards")
    void shouldHandleDeckWithMultipleFlashcards() {
        Flashcard card1 = new Flashcard(1L, 1L, "Front 1", "Back 1");
        Flashcard card2 = new Flashcard(2L, 1L, "Front 2", "Back 2");
        Flashcard card3 = new Flashcard(3L, 1L, "Front 3", "Back 3");

        testDeck.addFlashcard(card1);
        testDeck.addFlashcard(card2);
        testDeck.addFlashcard(card3);

        assertThat(testDeck.getFlashcardCount()).isEqualTo(3);
        assertThat(testDeck.getFlashcards()).hasSize(3);
    }

    @Test
    @DisplayName("Should handle deck with null flashcards list")
    void shouldHandleDeckWithNullFlashcardsList() {
        Deck deck = new Deck();
        deck.setFlashcards(null);

        assertThat(deck.getFlashcardCount()).isZero();
        assertThat(deck.getFlashcards()).isEmpty();
    }

    @Test
    @DisplayName("Should validate deck constraints")
    void shouldValidateDeckConstraints() {
        // Test title length constraint - @Size(max = 120) is JPA validation, not runtime
        String longTitle = "A".repeat(121);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testDeck.setTitle(longTitle);
        assertThat(testDeck.getTitle()).isEqualTo(longTitle);

        // Test description length constraint - @Size(max = 500) is JPA validation, not runtime
        String longDescription = "A".repeat(501);
        // This should not throw exception as @Size is JPA validation, not runtime validation
        testDeck.setDescription(longDescription);
        assertThat(testDeck.getDescription()).isEqualTo(longDescription);
    }

    @Test
    @DisplayName("Should handle deck with special characters")
    void shouldHandleDeckWithSpecialCharacters() {
        String specialTitle = "Deck with special chars: @#$%^&*()";
        Deck deck = Deck.create(1L, specialTitle, "Description");

        assertThat(deck.getTitle()).isEqualTo(specialTitle);
    }

    @Test
    @DisplayName("Should handle deck with unicode characters")
    void shouldHandleDeckWithUnicodeCharacters() {
        String unicodeTitle = "Deck with unicode characters: 你好世界";
        Deck deck = Deck.create(1L, unicodeTitle, "Test description");

        assertThat(deck.getTitle()).isEqualTo(unicodeTitle);
    }

    @Test
    @DisplayName("Should handle deck equality and hash code")
    void shouldHandleDeckEqualityAndHashCode() {
        Deck deck1 = new Deck(1L, 1L, "Title", "Description");
        Deck deck2 = new Deck(1L, 1L, "Title", "Description");
        Deck deck3 = new Deck(2L, 1L, "Title", "Description");

        assertThat(deck1).isEqualTo(deck2).isNotEqualTo(deck3).hasSameHashCodeAs(deck2);
        assertThat(deck1.hashCode()).isNotEqualTo(deck3.hashCode());
    }

    @Test
    @DisplayName("Should handle deck string representation")
    void shouldHandleDeckStringRepresentation() {
        String deckString = testDeck.toString();

        assertThat(deckString)
                .contains("Test Deck")
                .contains("Test Description")
                .contains("flashcardCount=0");
    }

    @Test
    @DisplayName("Should handle deck with maximum allowed values")
    void shouldHandleDeckWithMaximumAllowedValues() {
        String maxTitle = "A".repeat(120); // Maximum allowed title length
        String maxDescription = "A".repeat(500); // Maximum allowed description length

        testDeck.setTitle(maxTitle);
        testDeck.setDescription(maxDescription);

        assertThat(testDeck.getTitle()).isEqualTo(maxTitle);
        assertThat(testDeck.getDescription()).isEqualTo(maxDescription);
    }

    @Test
    @DisplayName("Should handle deck with minimum allowed values")
    void shouldHandleDeckWithMinimumAllowedValues() {
        String minTitle = "A"; // Minimum valid title length
        testDeck.setTitle(minTitle);

        assertThat(testDeck.getTitle()).isEqualTo(minTitle);
    }

    @Test
    @DisplayName("Should handle deck creation with null values")
    void shouldHandleDeckCreationWithNullValues() {
        Deck deck = new Deck();
        deck.setId(1L);
        deck.setUserId(1L);
        deck.setTitle("Valid Title");
        deck.setDescription(null);

        assertThat(deck).satisfies(d -> {
            assertThat(d.getId()).isEqualTo(1L);
            assertThat(d.getUserId()).isEqualTo(1L);
            assertThat(d.getTitle()).isEqualTo("Valid Title");
            assertThat(d.getDescription()).isNull();
        });
    }

    @Test
    @DisplayName("Should handle deck with complex title formats")
    void shouldHandleDeckWithComplexTitleFormats() {
        // Valid title formats
        testDeck.setTitle("Deck Title with Numbers 123");
        assertThat(testDeck.getTitle()).isEqualTo("Deck Title with Numbers 123");

        testDeck.setTitle("Deck-Title_with_Underscores");
        assertThat(testDeck.getTitle()).isEqualTo("Deck-Title_with_Underscores");

        testDeck.setTitle("Deck Title with Spaces");
        assertThat(testDeck.getTitle()).isEqualTo("Deck Title with Spaces");
    }
}
