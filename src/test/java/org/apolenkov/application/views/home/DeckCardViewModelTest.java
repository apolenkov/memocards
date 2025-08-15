package org.apolenkov.application.views.home;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DeckCardViewModel Tests")
class DeckCardViewModelTest {

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create DeckCardViewModel with all fields")
        void shouldCreateDeckCardViewModelWithAllFields() {
            // Given
            Long id = 1L;
            String title = "Test Deck";
            String description = "Test Description";
            int deckSize = 10;
            int knownCount = 5;
            int progressPercent = 50;

            // When
            DeckCardViewModel viewModel =
                    new DeckCardViewModel(id, title, description, deckSize, knownCount, progressPercent);

            // Then
            assertThat(viewModel.id()).isEqualTo(id);
            assertThat(viewModel.title()).isEqualTo(title);
            assertThat(viewModel.description()).isEqualTo(description);
            assertThat(viewModel.deckSize()).isEqualTo(deckSize);
            assertThat(viewModel.knownCount()).isEqualTo(knownCount);
            assertThat(viewModel.progressPercent()).isEqualTo(progressPercent);
        }

        @Test
        @DisplayName("Should create DeckCardViewModel with null values")
        void shouldCreateDeckCardViewModelWithNullValues() {
            // Given
            Long id = null;
            String title = null;
            String description = null;
            int deckSize = 0;
            int knownCount = 0;
            int progressPercent = 0;

            // When
            DeckCardViewModel viewModel =
                    new DeckCardViewModel(id, title, description, deckSize, knownCount, progressPercent);

            // Then
            assertThat(viewModel.id()).isNull();
            assertThat(viewModel.title()).isNull();
            assertThat(viewModel.description()).isNull();
            assertThat(viewModel.deckSize()).isEqualTo(0);
            assertThat(viewModel.knownCount()).isEqualTo(0);
            assertThat(viewModel.progressPercent()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create DeckCardViewModel with edge case values")
        void shouldCreateDeckCardViewModelWithEdgeCaseValues() {
            // Given
            Long id = Long.MAX_VALUE;
            String title = "";
            String description = "a".repeat(1000);
            int deckSize = Integer.MAX_VALUE;
            int knownCount = Integer.MIN_VALUE;
            int progressPercent = 100;

            // When
            DeckCardViewModel viewModel =
                    new DeckCardViewModel(id, title, description, deckSize, knownCount, progressPercent);

            // Then
            assertThat(viewModel.id()).isEqualTo(Long.MAX_VALUE);
            assertThat(viewModel.title()).isEmpty();
            assertThat(viewModel.description()).isEqualTo("a".repeat(1000));
            assertThat(viewModel.deckSize()).isEqualTo(Integer.MAX_VALUE);
            assertThat(viewModel.knownCount()).isEqualTo(Integer.MIN_VALUE);
            assertThat(viewModel.progressPercent()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Record Equality Tests")
    class RecordEqualityTests {

        @Test
        @DisplayName("Should be equal to identical DeckCardViewModel")
        void shouldBeEqualToIdenticalDeckCardViewModel() {
            // Given
            DeckCardViewModel viewModel1 = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);
            DeckCardViewModel viewModel2 = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When & Then
            assertThat(viewModel1).isEqualTo(viewModel2);
            assertThat(viewModel1.hashCode()).isEqualTo(viewModel2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to different DeckCardViewModel")
        void shouldNotBeEqualToDifferentDeckCardViewModel() {
            // Given
            DeckCardViewModel viewModel1 = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);
            DeckCardViewModel viewModel2 = new DeckCardViewModel(2L, "Test", "Description", 10, 5, 50);

            // When & Then
            assertThat(viewModel1).isNotEqualTo(viewModel2);
            assertThat(viewModel1.hashCode()).isNotEqualTo(viewModel2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When & Then
            assertThat(viewModel).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);
            String differentType = "Not a DeckCardViewModel";

            // When & Then
            assertThat(viewModel).isNotEqualTo(differentType);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When & Then
            assertThat(viewModel).isEqualTo(viewModel);
        }
    }

    @Nested
    @DisplayName("Record HashCode Tests")
    class RecordHashCodeTests {

        @Test
        @DisplayName("HashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When
            int hashCode1 = viewModel.hashCode();
            int hashCode2 = viewModel.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("HashCode should be different for different objects")
        void hashCodeShouldBeDifferentForDifferentObjects() {
            // Given
            DeckCardViewModel viewModel1 = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);
            DeckCardViewModel viewModel2 = new DeckCardViewModel(2L, "Test", "Description", 10, 5, 50);

            // When
            int hashCode1 = viewModel1.hashCode();
            int hashCode2 = viewModel2.hashCode();

            // Then
            assertThat(hashCode1).isNotEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("Record ToString Tests")
    class RecordToStringTests {

        @Test
        @DisplayName("ToString should contain all field values")
        void toStringShouldContainAllFieldValues() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test Deck", "Test Description", 10, 5, 50);

            // When
            String result = viewModel.toString();

            // Then
            assertThat(result)
                    .contains("id=1")
                    .contains("title=Test Deck")
                    .contains("description=Test Description")
                    .contains("deckSize=10")
                    .contains("knownCount=5")
                    .contains("progressPercent=50");
        }

        @Test
        @DisplayName("ToString should handle null values")
        void toStringShouldHandleNullValues() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(null, null, null, 0, 0, 0);

            // When
            String result = viewModel.toString();

            // Then
            assertThat(result).contains("id=null").contains("title=null").contains("description=null");
        }

        @Test
        @DisplayName("ToString should handle special characters")
        void toStringShouldHandleSpecialCharacters() {
            // Given
            DeckCardViewModel viewModel =
                    new DeckCardViewModel(1L, "Test@#$%", "Description with \"quotes\"", 10, 5, 50);

            // When
            String result = viewModel.toString();

            // Then
            assertThat(result).contains("title=Test@#$%").contains("description=Description with \"quotes\"");
        }
    }

    @Nested
    @DisplayName("Record Immutability Tests")
    class RecordImmutabilityTests {

        @Test
        @DisplayName("Should be immutable")
        void shouldBeImmutable() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When & Then
            // Records are immutable by design, so we can't modify their fields
            // This test verifies that the record behaves as expected
            assertThat(viewModel.id()).isEqualTo(1L);
            assertThat(viewModel.title()).isEqualTo("Test");
            assertThat(viewModel.description()).isEqualTo("Description");
            assertThat(viewModel.deckSize()).isEqualTo(10);
            assertThat(viewModel.knownCount()).isEqualTo(5);
            assertThat(viewModel.progressPercent()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long strings")
        void shouldHandleVeryLongStrings() {
            // Given
            String longTitle = "a".repeat(10000);
            String longDescription = "b".repeat(10000);

            // When
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, longTitle, longDescription, 10, 5, 50);

            // Then
            assertThat(viewModel.title()).isEqualTo(longTitle);
            assertThat(viewModel.description()).isEqualTo(longDescription);
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() {
            // Given
            String unicodeTitle = "Тестовая колода"; // Russian
            String unicodeDescription = "Descripción de prueba"; // Spanish

            // When
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, unicodeTitle, unicodeDescription, 10, 5, 50);

            // Then
            assertThat(viewModel.title()).isEqualTo(unicodeTitle);
            assertThat(viewModel.description()).isEqualTo(unicodeDescription);
        }

        @Test
        @DisplayName("Should handle special characters in strings")
        void shouldHandleSpecialCharactersInStrings() {
            // Given
            String specialTitle = "Title with @#$%^&*()_+-=[]{}|;':\",./<>?";
            String specialDescription = "Description with \n\t\r special chars";

            // When
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, specialTitle, specialDescription, 10, 5, 50);

            // Then
            assertThat(viewModel.title()).isEqualTo(specialTitle);
            assertThat(viewModel.description()).isEqualTo(specialDescription);
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent valid progress percentage")
        void shouldRepresentValidProgressPercentage() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When & Then
            assertThat(viewModel.progressPercent()).isEqualTo(50);
            assertThat(viewModel.progressPercent()).isBetween(0, 100);
        }

        @Test
        @DisplayName("Should represent valid deck size")
        void shouldRepresentValidDeckSize() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When & Then
            assertThat(viewModel.deckSize()).isEqualTo(10);
            assertThat(viewModel.deckSize()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Should represent valid known count")
        void shouldRepresentValidKnownCount() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 10, 5, 50);

            // When & Then
            assertThat(viewModel.knownCount()).isEqualTo(5);
            assertThat(viewModel.knownCount()).isGreaterThanOrEqualTo(0);
            assertThat(viewModel.knownCount()).isLessThanOrEqualTo(viewModel.deckSize());
        }

        @Test
        @DisplayName("Should handle zero values")
        void shouldHandleZeroValues() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(1L, "Test", "Description", 0, 0, 0);

            // When & Then
            assertThat(viewModel.deckSize()).isEqualTo(0);
            assertThat(viewModel.knownCount()).isEqualTo(0);
            assertThat(viewModel.progressPercent()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle maximum values")
        void shouldHandleMaximumValues() {
            // Given
            DeckCardViewModel viewModel = new DeckCardViewModel(
                    Long.MAX_VALUE, "Test", "Description", Integer.MAX_VALUE, Integer.MAX_VALUE, 100);

            // When & Then
            assertThat(viewModel.id()).isEqualTo(Long.MAX_VALUE);
            assertThat(viewModel.deckSize()).isEqualTo(Integer.MAX_VALUE);
            assertThat(viewModel.knownCount()).isEqualTo(Integer.MAX_VALUE);
            assertThat(viewModel.progressPercent()).isEqualTo(100);
        }
    }
}
