package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("News Model Tests")
class NewsTest {

    private News news;
    private final LocalDateTime testTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        news = new News(1L, "Test News", "Test Content", "Test Author", testTime);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should set all fields correctly")
        void constructorShouldSetAllFieldsCorrectly() {
            // Given
            long id = 1L;
            String title = "Test Title";
            String content = "Test Content";
            String author = "Test Author";
            LocalDateTime createdAt = testTime;

            // When
            News newNews = new News(id, title, content, author, createdAt);

            // Then
            assertThat(newNews.getId()).isEqualTo(id);
            assertThat(newNews.getTitle()).isEqualTo(title);
            assertThat(newNews.getContent()).isEqualTo(content);
            assertThat(newNews.getAuthor()).isEqualTo(author);
            assertThat(newNews.getCreatedAt()).isEqualTo(createdAt);
            assertThat(newNews.getUpdatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Constructor should handle null values")
        void constructorShouldHandleNullValues() {
            // When
            News newNews = new News(null, null, null, null, null);

            // Then
            assertThat(newNews.getId()).isNull();
            assertThat(newNews.getTitle()).isNull();
            assertThat(newNews.getContent()).isNull();
            assertThat(newNews.getAuthor()).isNull();
            assertThat(newNews.getCreatedAt()).isNull();
            assertThat(newNews.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("Constructor should set updatedAt to same as createdAt")
        void constructorShouldSetUpdatedAtToSameAsCreatedAt() {
            // Given
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);

            // When
            News newNews = new News(1L, "Title", "Content", "Author", createdAt);

            // Then
            assertThat(newNews.getUpdatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Id getter and setter should work correctly")
        void idGetterAndSetterShouldWorkCorrectly() {
            // Given
            long newId = 999L;

            // When
            news.setId(newId);

            // Then
            assertThat(news.getId()).isEqualTo(newId);
        }

        @Test
        @DisplayName("Title getter and setter should work correctly")
        void titleGetterAndSetterShouldWorkCorrectly() {
            // Given
            String newTitle = "New Title";

            // When
            news.setTitle(newTitle);

            // Then
            assertThat(news.getTitle()).isEqualTo(newTitle);
        }

        @Test
        @DisplayName("Content getter and setter should work correctly")
        void contentGetterAndSetterShouldWorkCorrectly() {
            // Given
            String newContent = "New Content";

            // When
            news.setContent(newContent);

            // Then
            assertThat(news.getContent()).isEqualTo(newContent);
        }

        @Test
        @DisplayName("Author getter and setter should work correctly")
        void authorGetterAndSetterShouldWorkCorrectly() {
            // Given
            String newAuthor = "New Author";

            // When
            news.setAuthor(newAuthor);

            // Then
            assertThat(news.getAuthor()).isEqualTo(newAuthor);
        }

        @Test
        @DisplayName("CreatedAt getter and setter should work correctly")
        void createdAtGetterAndSetterShouldWorkCorrectly() {
            // Given
            LocalDateTime newCreatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);

            // When
            news.setCreatedAt(newCreatedAt);

            // Then
            assertThat(news.getCreatedAt()).isEqualTo(newCreatedAt);
        }

        @Test
        @DisplayName("UpdatedAt getter and setter should work correctly")
        void updatedAtGetterAndSetterShouldWorkCorrectly() {
            // Given
            LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);

            // When
            news.setUpdatedAt(newUpdatedAt);

            // Then
            assertThat(news.getUpdatedAt()).isEqualTo(newUpdatedAt);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longTitle = "a".repeat(1000);
            String longContent = "b".repeat(10000);
            String longAuthor = "c".repeat(500);

            // When
            news.setTitle(longTitle);
            news.setContent(longContent);
            news.setAuthor(longAuthor);

            // Then
            assertThat(news.getTitle()).isEqualTo(longTitle);
            assertThat(news.getContent()).isEqualTo(longContent);
            assertThat(news.getAuthor()).isEqualTo(longAuthor);
        }

        @Test
        @DisplayName("Should handle very large IDs")
        void shouldHandleVeryLargeIds() {
            // Given
            long largeId = Long.MAX_VALUE;

            // When
            news.setId(largeId);

            // Then
            assertThat(news.getId()).isEqualTo(largeId);
        }

        @Test
        @DisplayName("Should handle negative IDs")
        void shouldHandleNegativeIds() {
            // Given
            long negativeId = -1L;

            // When
            news.setId(negativeId);

            // Then
            assertThat(news.getId()).isEqualTo(negativeId);
        }

        @Test
        @DisplayName("Should handle zero ID")
        void shouldHandleZeroId() {
            // Given
            long zeroId = 0L;

            // When
            news.setId(zeroId);

            // Then
            assertThat(news.getId()).isZero();
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Given
            String emptyTitle = "";
            String emptyContent = "";
            String emptyAuthor = "";

            // When
            news.setTitle(emptyTitle);
            news.setContent(emptyContent);
            news.setAuthor(emptyAuthor);

            // Then
            assertThat(news.getTitle()).isEmpty();
            assertThat(news.getContent()).isEmpty();
            assertThat(news.getAuthor()).isEmpty();
        }

        @Test
        @DisplayName("Should handle whitespace strings")
        void shouldHandleWhitespaceStrings() {
            // Given
            String whitespaceTitle = "   ";
            String whitespaceContent = "\t\n\r";
            String whitespaceAuthor = "  \t  ";

            // When
            news.setTitle(whitespaceTitle);
            news.setContent(whitespaceContent);
            news.setAuthor(whitespaceAuthor);

            // Then
            assertThat(news.getTitle()).isEqualTo(whitespaceTitle);
            assertThat(news.getContent()).isEqualTo(whitespaceContent);
            assertThat(news.getAuthor()).isEqualTo(whitespaceAuthor);
        }
    }

    @Nested
    @DisplayName("Special Characters Tests")
    class SpecialCharactersTests {

        @Test
        @DisplayName("Should handle special characters in title")
        void shouldHandleSpecialCharactersInTitle() {
            // Given
            String specialTitle = "Title with @#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            news.setTitle(specialTitle);

            // Then
            assertThat(news.getTitle()).isEqualTo(specialTitle);
        }

        @Test
        @DisplayName("Should handle special characters in content")
        void shouldHandleSpecialCharactersInContent() {
            // Given
            String specialContent = "Content with \n\t\r special chars and \"quotes\"";

            // When
            news.setContent(specialContent);

            // Then
            assertThat(news.getContent()).isEqualTo(specialContent);
        }

        @Test
        @DisplayName("Should handle special characters in author")
        void shouldHandleSpecialCharactersInAuthor() {
            // Given
            String specialAuthor = "Author with @#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            news.setAuthor(specialAuthor);

            // Then
            assertThat(news.getAuthor()).isEqualTo(specialAuthor);
        }
    }

    @Nested
    @DisplayName("Unicode Tests")
    class UnicodeTests {

        @Test
        @DisplayName("Should handle unicode characters in title")
        void shouldHandleUnicodeCharactersInTitle() {
            // Given
            String unicodeTitle = "Новости с кириллицей"; // Russian

            // When
            news.setTitle(unicodeTitle);

            // Then
            assertThat(news.getTitle()).isEqualTo(unicodeTitle);
        }

        @Test
        @DisplayName("Should handle unicode characters in content")
        void shouldHandleUnicodeCharactersInContent() {
            // Given
            String unicodeContent = "Contenido con caracteres especiales"; // Spanish

            // When
            news.setContent(unicodeContent);

            // Then
            assertThat(news.getContent()).isEqualTo(unicodeContent);
        }

        @Test
        @DisplayName("Should handle unicode characters in author")
        void shouldHandleUnicodeCharactersInAuthor() {
            // Given
            String unicodeAuthor = "José María O'Connor-Smith";

            // When
            news.setAuthor(unicodeAuthor);

            // Then
            assertThat(news.getAuthor()).isEqualTo(unicodeAuthor);
        }

        @Test
        @DisplayName("Should handle emoji characters")
        void shouldHandleEmojiCharacters() {
            // Given
            String emojiTitle = "News with 🚀 emojis 🎉";
            String emojiContent = "Content with 📱 📚 🎯 emojis";
            String emojiAuthor = "Author 👨‍💻 with emojis";

            // When
            news.setTitle(emojiTitle);
            news.setContent(emojiContent);
            news.setAuthor(emojiAuthor);

            // Then
            assertThat(news.getTitle()).isEqualTo(emojiTitle);
            assertThat(news.getContent()).isEqualTo(emojiContent);
            assertThat(news.getAuthor()).isEqualTo(emojiAuthor);
        }
    }

    @Nested
    @DisplayName("DateTime Tests")
    class DateTimeTests {

        @Test
        @DisplayName("Should handle different time zones")
        void shouldHandleDifferentTimeZones() {
            // Given
            LocalDateTime utcTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            LocalDateTime localTime = LocalDateTime.now();

            // When
            news.setCreatedAt(utcTime);
            news.setUpdatedAt(localTime);

            // Then
            assertThat(news.getCreatedAt()).isEqualTo(utcTime);
            assertThat(news.getUpdatedAt()).isEqualTo(localTime);
        }

        @Test
        @DisplayName("Should handle future dates")
        void shouldHandleFutureDates() {
            // Given
            LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);

            // When
            news.setCreatedAt(futureDate);
            news.setUpdatedAt(futureDate);

            // Then
            assertThat(news.getCreatedAt()).isEqualTo(futureDate);
            assertThat(news.getUpdatedAt()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("Should handle past dates")
        void shouldHandlePastDates() {
            // Given
            LocalDateTime pastDate = LocalDateTime.of(1990, 1, 1, 0, 0);

            // When
            news.setCreatedAt(pastDate);
            news.setUpdatedAt(pastDate);

            // Then
            assertThat(news.getCreatedAt()).isEqualTo(pastDate);
            assertThat(news.getUpdatedAt()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("Should handle leap year dates")
        void shouldHandleLeapYearDates() {
            // Given
            LocalDateTime leapYearDate = LocalDateTime.of(2024, 2, 29, 12, 0);

            // When
            news.setCreatedAt(leapYearDate);
            news.setUpdatedAt(leapYearDate);

            // Then
            assertThat(news.getCreatedAt()).isEqualTo(leapYearDate);
            assertThat(news.getUpdatedAt()).isEqualTo(leapYearDate);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should maintain separate created and updated timestamps")
        void shouldMaintainSeparateCreatedAndUpdatedTimestamps() {
            // Given
            LocalDateTime originalCreatedAt = news.getCreatedAt();
            LocalDateTime newUpdatedAt = LocalDateTime.now();

            // When
            news.setUpdatedAt(newUpdatedAt);

            // Then
            assertThat(news.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(news.getUpdatedAt()).isEqualTo(newUpdatedAt);
            assertThat(news.getCreatedAt()).isNotEqualTo(news.getUpdatedAt());
        }

        @Test
        @DisplayName("Should allow updating all fields independently")
        void shouldAllowUpdatingAllFieldsIndependently() {
            // Given
            long newId = 999L;
            String newTitle = "Updated Title";
            String newContent = "Updated Content";
            String newAuthor = "Updated Author";
            LocalDateTime newCreatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);
            LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 2, 1, 13, 0);

            // When
            news.setId(newId);
            news.setTitle(newTitle);
            news.setContent(newContent);
            news.setAuthor(newAuthor);
            news.setCreatedAt(newCreatedAt);
            news.setUpdatedAt(newUpdatedAt);

            // Then
            assertThat(news.getId()).isEqualTo(newId);
            assertThat(news.getTitle()).isEqualTo(newTitle);
            assertThat(news.getContent()).isEqualTo(newContent);
            assertThat(news.getAuthor()).isEqualTo(newAuthor);
            assertThat(news.getCreatedAt()).isEqualTo(newCreatedAt);
            assertThat(news.getUpdatedAt()).isEqualTo(newUpdatedAt);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should create complete news object")
        void shouldCreateCompleteNewsObject() {
            // Given
            long id = 1L;
            String title = "Breaking News";
            String content = "This is a breaking news story with important information.";
            String author = "John Doe";
            LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 12, 0);

            // When
            News completeNews = new News(id, title, content, author, createdAt);

            // Then
            assertThat(completeNews.getId()).isEqualTo(id);
            assertThat(completeNews.getTitle()).isEqualTo(title);
            assertThat(completeNews.getContent()).isEqualTo(content);
            assertThat(completeNews.getAuthor()).isEqualTo(author);
            assertThat(completeNews.getCreatedAt()).isEqualTo(createdAt);
            assertThat(completeNews.getUpdatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("Should update news object completely")
        void shouldUpdateNewsObjectCompletely() {
            // Given
            News originalNews = new News(1L, "Original Title", "Original Content", "Original Author", testTime);
            long newId = 2L;
            String newTitle = "New Title";
            String newContent = "New Content";
            String newAuthor = "New Author";
            LocalDateTime newCreatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);
            LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 2, 1, 13, 0);

            // When
            originalNews.setId(newId);
            originalNews.setTitle(newTitle);
            originalNews.setContent(newContent);
            originalNews.setAuthor(newAuthor);
            originalNews.setCreatedAt(newCreatedAt);
            originalNews.setUpdatedAt(newUpdatedAt);

            // Then
            assertThat(originalNews.getId()).isEqualTo(newId);
            assertThat(originalNews.getTitle()).isEqualTo(newTitle);
            assertThat(originalNews.getContent()).isEqualTo(newContent);
            assertThat(originalNews.getAuthor()).isEqualTo(newAuthor);
            assertThat(originalNews.getCreatedAt()).isEqualTo(newCreatedAt);
            assertThat(originalNews.getUpdatedAt()).isEqualTo(newUpdatedAt);
        }
    }
}
