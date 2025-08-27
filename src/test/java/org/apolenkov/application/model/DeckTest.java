package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Deck Model Tests")
class DeckTest {

    private Deck deck;
    private final LocalDateTime testTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        deck = new Deck();
        deck.setId(1L); // Ensure deck has ID for flashcard operations
        deck.setUserId(2L);
        deck.setTitle("Test Title");
        deck.setDescription("Test Description");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize with current time")
        void defaultConstructorShouldInitializeWithCurrentTime() {
            Deck newDeck = new Deck();

            assertThat(newDeck.getId()).isNull();
            assertThat(newDeck.getUserId()).isNull();
            assertThat(newDeck.getTitle()).isNull();
            assertThat(newDeck.getDescription()).isNull();
            assertThat(newDeck.getFlashcards()).isEmpty();
            assertThat(newDeck.getCreatedAt())
                    .isCloseTo(LocalDateTime.now(), within(1, java.time.temporal.ChronoUnit.SECONDS));
            assertThat(newDeck.getUpdatedAt())
                    .isCloseTo(LocalDateTime.now(), within(1, java.time.temporal.ChronoUnit.SECONDS));
        }

        @Test
        @DisplayName("Parameterized constructor should set all fields")
        void parameterizedConstructorShouldSetAllFields() {
            Deck newDeck = new Deck(1L, 2L, "Test Title", "Test Description");

            assertThat(newDeck.getId()).isEqualTo(1L);
            assertThat(newDeck.getUserId()).isEqualTo(2L);
            assertThat(newDeck.getTitle()).isEqualTo("Test Title");
            assertThat(newDeck.getDescription()).isEqualTo("Test Description");
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Create should return valid deck with trimmed values")
        void createShouldReturnValidDeckWithTrimmedValues() {
            Deck created = Deck.create(1L, "  Test Title  ", "  Test Description  ");

            assertThat(created.getUserId()).isEqualTo(1L);
            assertThat(created.getTitle()).isEqualTo("Test Title");
            assertThat(created.getDescription()).isEqualTo("Test Description");
            assertThat(created.getCreatedAt()).isNotNull();
            assertThat(created.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Create should throw exception for null userId")
        void createShouldThrowExceptionForNullUserId() {
            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> Deck.create(null, "Title", "Description"));
            assertThat(ex).hasMessageContaining("userId");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Create should throw exception for null or empty title")
        void createShouldThrowExceptionForNullOrEmptyTitle(final String title) {
            IllegalArgumentException ex =
                    assertThrows(IllegalArgumentException.class, () -> Deck.create(1L, title, "Description"));
            assertThat(ex).hasMessage("title is required");
        }

        @Test
        @DisplayName("Create should handle null description")
        void createShouldHandleNullDescription() {
            Deck created = Deck.create(1L, "Title", null);

            assertThat(created.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Id getter and setter should work correctly")
        void idGetterAndSetterShouldWorkCorrectly() {
            deck.setId(123L);
            assertThat(deck.getId()).isEqualTo(123L);
        }

        @Test
        @DisplayName("UserId getter and setter should work correctly")
        void userIdGetterAndSetterShouldWorkCorrectly() {
            deck.setUserId(456L);
            assertThat(deck.getUserId()).isEqualTo(456L);
        }

        @Test
        @DisplayName("UserId setter should throw exception for null")
        void userIdSetterShouldThrowExceptionForNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deck.setUserId(null));
            assertThat(ex).hasMessage("userId is required");
        }

        @Test
        @DisplayName("Title getter and setter should work correctly")
        void titleGetterAndSetterShouldWorkCorrectly() {
            deck.setTitle("New Title");
            assertThat(deck.getTitle()).isEqualTo("New Title");
        }

        @Test
        @DisplayName("Title setter should trim whitespace")
        void titleSetterShouldTrimWhitespace() {
            deck.setTitle("  Trimmed Title  ");
            assertThat(deck.getTitle()).isEqualTo("Trimmed Title");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Title setter should throw exception for null or empty title")
        void titleSetterShouldThrowExceptionForNullOrEmptyTitle(final String title) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> deck.setTitle(title));
            assertThat(ex).hasMessage("title is required");
        }

        @Test
        @DisplayName("Description getter and setter should work correctly")
        void descriptionGetterAndSetterShouldWorkCorrectly() {
            deck.setDescription("New Description");
            assertThat(deck.getDescription()).isEqualTo("New Description");
        }

        @Test
        @DisplayName("Description setter should trim whitespace")
        void descriptionSetterShouldTrimWhitespace() {
            deck.setDescription("  Trimmed Description  ");
            assertThat(deck.getDescription()).isEqualTo("Trimmed Description");
        }

        @Test
        @DisplayName("Description setter should handle null")
        void descriptionSetterShouldHandleNull() {
            deck.setDescription(null);
            assertThat(deck.getDescription()).isNull();
        }

        @Test
        @DisplayName("CreatedAt getter and setter should work correctly")
        void createdAtGetterAndSetterShouldWorkCorrectly() {
            deck.setCreatedAt(testTime);
            assertThat(deck.getCreatedAt()).isEqualTo(testTime);
        }

        @Test
        @DisplayName("UpdatedAt getter and setter should work correctly")
        void updatedAtGetterAndSetterShouldWorkCorrectly() {
            deck.setUpdatedAt(testTime);
            assertThat(deck.getUpdatedAt()).isEqualTo(testTime);
        }
    }

    @Nested
    @DisplayName("Flashcard Management Tests")
    class FlashcardManagementTests {

        @Test
        @DisplayName("GetFlashcardCount should return correct count")
        void getFlashcardCountShouldReturnCorrectCount() {
            assertThat(deck.getFlashcardCount()).isZero();

            deck.setFlashcards(List.of(new Flashcard(), new Flashcard()));
            assertThat(deck.getFlashcardCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("AddFlashcard should add flashcard and update timestamp")
        void addFlashcardShouldAddFlashcardAndUpdateTimestamp() {
            Flashcard flashcard = new Flashcard();
            LocalDateTime beforeAdd = deck.getUpdatedAt();

            // Wait a bit to ensure timestamp difference
            await().atMost(java.time.Duration.ofMillis(100));

            deck.addFlashcard(flashcard);

            assertThat(deck.getFlashcards()).hasSize(1);
            assertThat(deck.getFlashcards().getFirst()).isEqualTo(flashcard);
            assertThat(deck.getUpdatedAt()).isAfter(beforeAdd);
            assertThat(flashcard.getDeckId()).isEqualTo(deck.getId());
        }

        @Test
        @DisplayName("AddFlashcard should throw exception for null flashcard")
        void addFlashcardShouldThrowExceptionForNullFlashcard() {
            assertThatThrownBy(() -> deck.addFlashcard(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("flashcard is null");
        }

        @Test
        @DisplayName("RemoveFlashcard should remove flashcard and update timestamp")
        void removeFlashcardShouldRemoveFlashcardAndUpdateTimestamp() {
            Flashcard flashcard = new Flashcard();
            deck.addFlashcard(flashcard);
            LocalDateTime beforeRemove = deck.getUpdatedAt();

            // Wait a bit to ensure timestamp difference
            await().atMost(java.time.Duration.ofMillis(100));

            deck.removeFlashcard(flashcard);

            assertThat(deck.getFlashcards()).isEmpty();
            assertThat(deck.getUpdatedAt()).isAfter(beforeRemove);
        }

        @Test
        @DisplayName("RemoveFlashcard should handle null flashcards list")
        void removeFlashcardShouldHandleNullFlashcardsList() {
            deck.setFlashcards(null);
            assertThatNoException().isThrownBy(() -> deck.removeFlashcard(new Flashcard()));
        }

        @Test
        @DisplayName("SetFlashcards should create new list")
        void setFlashcardsShouldCreateNewList() {
            List<Flashcard> flashcards = List.of(new Flashcard(), new Flashcard());
            deck.setFlashcards(flashcards);

            assertThat(deck.getFlashcards()).hasSize(2);
            assertThat(deck.getFlashcards()).isNotSameAs(flashcards);
        }

        @Test
        @DisplayName("SetFlashcards should handle null input")
        void setFlashcardsShouldHandleNullInput() {
            deck.setFlashcards(null);
            assertThat(deck.getFlashcards()).isEmpty();
        }

        @Test
        @DisplayName("GetFlashcards should return unmodifiable list")
        void getFlashcardsShouldReturnUnmodifiableList() {
            deck.setFlashcards(List.of(new Flashcard()));

            List<Flashcard> list = deck.getFlashcards();
            Flashcard newCard = new Flashcard();

            assertThatThrownBy(() -> list.add(newCard)).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Timestamp Update Tests")
    class TimestampUpdateTests {

        @Test
        @DisplayName("Title setter should update timestamp")
        void titleSetterShouldUpdateTimestamp() {
            LocalDateTime beforeUpdate = deck.getUpdatedAt();
            await().atMost(java.time.Duration.ofMillis(100));

            deck.setTitle("New Title");

            assertThat(deck.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("Description setter should update timestamp")
        void descriptionSetterShouldUpdateTimestamp() {
            LocalDateTime beforeUpdate = deck.getUpdatedAt();
            await().atMost(java.time.Duration.ofMillis(100));

            deck.setDescription("New Description");

            assertThat(deck.getUpdatedAt()).isAfter(beforeUpdate);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Equals should work correctly")
        void equalsShouldWorkCorrectly() {
            Deck deck1 = new Deck();
            deck1.setId(1L);

            Deck deck2 = new Deck();
            deck2.setId(1L);

            Deck deck3 = new Deck();
            deck3.setId(2L);

            assertThat(deck1).isEqualTo(deck2).isNotEqualTo(deck3).isNotEqualTo(null);
        }

        @Test
        @DisplayName("HashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            Deck deck1 = new Deck();
            deck1.setId(1L);

            Deck deck2 = new Deck();
            deck2.setId(1L);

            assertThat(deck1).hasSameHashCodeAs(deck2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("ToString should contain all relevant information")
        void toStringShouldContainAllRelevantInformation() {
            deck.setId(1L);
            deck.setUserId(2L);
            deck.setTitle("Test Title");
            deck.setDescription("Test Description");
            deck.setCreatedAt(testTime);
            deck.setUpdatedAt(testTime);

            String result = deck.toString();

            assertThat(result)
                    .contains("id=1")
                    .contains("userId=2")
                    .contains("title='Test Title'")
                    .contains("description='Test Description'")
                    .contains("flashcardCount=0")
                    .contains("createdAt=" + testTime)
                    .contains("updatedAt=" + testTime);
        }
    }
}
