package org.apolenkov.application.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PracticeDirection Enum Tests")
class PracticeDirectionTest {

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have exactly two values")
        void shouldHaveExactlyTwoValues() {
            // When
            PracticeDirection[] values = PracticeDirection.values();

            // Then
            assertThat(values).hasSize(2);
        }

        @Test
        @DisplayName("Should contain FRONT_TO_BACK value")
        void shouldContainFrontToBackValue() {
            // When
            PracticeDirection[] values = PracticeDirection.values();

            // Then
            assertThat(values).contains(PracticeDirection.FRONT_TO_BACK);
        }

        @Test
        @DisplayName("Should contain BACK_TO_FRONT value")
        void shouldContainBackToFrontValue() {
            // When
            PracticeDirection[] values = PracticeDirection.values();

            // Then
            assertThat(values).contains(PracticeDirection.BACK_TO_FRONT);
        }
    }

    @Nested
    @DisplayName("Enum Ordinal Tests")
    class EnumOrdinalTests {

        @Test
        @DisplayName("FRONT_TO_BACK should have ordinal 0")
        void frontToBackShouldHaveOrdinalZero() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK.ordinal()).isZero();
        }

        @Test
        @DisplayName("BACK_TO_FRONT should have ordinal 1")
        void backToFrontShouldHaveOrdinalOne() {
            // When & Then
            assertThat(PracticeDirection.BACK_TO_FRONT.ordinal()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Enum Name Tests")
    class EnumNameTests {

        @Test
        @DisplayName("FRONT_TO_BACK should have correct name")
        void frontToBackShouldHaveCorrectName() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK.name()).isEqualTo("FRONT_TO_BACK");
        }

        @Test
        @DisplayName("BACK_TO_FRONT should have correct name")
        void backToFrontShouldHaveCorrectName() {
            // When & Then
            assertThat(PracticeDirection.BACK_TO_FRONT.name()).isEqualTo("BACK_TO_FRONT");
        }
    }

    @Nested
    @DisplayName("Enum ValueOf Tests")
    class EnumValueOfTests {

        @Test
        @DisplayName("ValueOf should return FRONT_TO_BACK for valid string")
        void valueOfShouldReturnFrontToBackForValidString() {
            // When
            PracticeDirection result = PracticeDirection.valueOf("FRONT_TO_BACK");

            // Then
            assertThat(result).isEqualTo(PracticeDirection.FRONT_TO_BACK);
        }

        @Test
        @DisplayName("ValueOf should return BACK_TO_FRONT for valid string")
        void valueOfShouldReturnBackToFrontForValidString() {
            // When
            PracticeDirection result = PracticeDirection.valueOf("BACK_TO_FRONT");

            // Then
            assertThat(result).isEqualTo(PracticeDirection.BACK_TO_FRONT);
        }

        @Test
        @DisplayName("ValueOf should throw exception for invalid string")
        void valueOfShouldThrowExceptionForInvalidString() {
            // When & Then
            assertThatThrownBy(() -> PracticeDirection.valueOf("INVALID_DIRECTION"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ValueOf should throw exception for null string")
        void valueOfShouldThrowExceptionForNullString() {
            // When & Then
            assertThatThrownBy(() -> PracticeDirection.valueOf(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Enum Comparison Tests")
    class EnumComparisonTests {

        @Test
        @DisplayName("FRONT_TO_BACK should be less than BACK_TO_FRONT")
        void frontToBackShouldBeLessThanBackToFront() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK.compareTo(PracticeDirection.BACK_TO_FRONT))
                    .isLessThan(0);
        }

        @Test
        @DisplayName("BACK_TO_FRONT should be greater than FRONT_TO_BACK")
        void backToFrontShouldBeGreaterThanFrontToBack() {
            // When & Then
            assertThat(PracticeDirection.BACK_TO_FRONT.compareTo(PracticeDirection.FRONT_TO_BACK))
                    .isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Enum Identity Tests")
    class EnumIdentityTests {

        @Test
        @DisplayName("Enum values should be singleton instances")
        void enumValuesShouldBeSingletonInstances() {
            // When
            PracticeDirection frontToBack1 = PracticeDirection.FRONT_TO_BACK;
            PracticeDirection frontToBack2 = PracticeDirection.FRONT_TO_BACK;
            PracticeDirection backToFront1 = PracticeDirection.BACK_TO_FRONT;
            PracticeDirection backToFront2 = PracticeDirection.BACK_TO_FRONT;

            // Then
            assertThat(frontToBack1).isSameAs(frontToBack2);
            assertThat(backToFront1).isSameAs(backToFront2);
        }

        @Test
        @DisplayName("Different enum values should not be the same instance")
        void differentEnumValuesShouldNotBeTheSameInstance() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK).isNotSameAs(PracticeDirection.BACK_TO_FRONT);
        }
    }

    @Nested
    @DisplayName("Enum ToString Tests")
    class EnumToStringTests {

        @Test
        @DisplayName("ToString should return the enum name")
        void toStringShouldReturnTheEnumName() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK).hasToString("FRONT_TO_BACK");
            assertThat(PracticeDirection.BACK_TO_FRONT).hasToString("BACK_TO_FRONT");
        }
    }

    @Nested
    @DisplayName("Enum HashCode Tests")
    class EnumHashCodeTests {

        @Test
        @DisplayName("HashCode should be consistent")
        void hashCodeShouldBeConsistent() {
            // When
            int hashCode1 = PracticeDirection.FRONT_TO_BACK.hashCode();
            int hashCode2 = PracticeDirection.FRONT_TO_BACK.hashCode();

            // Then
            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Different enum values should have different hash codes")
        void differentEnumValuesShouldHaveDifferentHashCodes() {
            // When
            int frontToBackHashCode = PracticeDirection.FRONT_TO_BACK.hashCode();
            int backToFrontHashCode = PracticeDirection.BACK_TO_FRONT.hashCode();

            // Then
            assertThat(frontToBackHashCode).isNotEqualTo(backToFrontHashCode);
        }
    }

    @Nested
    @DisplayName("Enum Equals Tests")
    class EnumEqualsTests {

        @Test
        @DisplayName("Equals should work correctly")
        void equalsShouldWorkCorrectly() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK).isNotEqualTo(PracticeDirection.BACK_TO_FRONT);
        }

        @Test
        @DisplayName("Equals should not be equal to null")
        void equalsShouldNotBeEqualToNull() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Equals should not be equal to different type")
        void equalsShouldNotBeEqualToDifferentType() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK).isNotEqualTo("FRONT_TO_BACK");
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("FRONT_TO_BACK should represent front to back practice")
        void frontToBackShouldRepresentFrontToBackPractice() {
            // When & Then
            assertThat(PracticeDirection.FRONT_TO_BACK.name()).contains("FRONT").contains("BACK");
        }

        @Test
        @DisplayName("BACK_TO_FRONT should represent back to front practice")
        void backToFrontShouldRepresentBackToFrontPractice() {
            // When & Then
            assertThat(PracticeDirection.BACK_TO_FRONT.name()).contains("BACK").contains("FRONT");
        }

        @Test
        @DisplayName("Enum should represent valid practice directions")
        void enumShouldRepresentValidPracticeDirections() {
            // When
            PracticeDirection[] values = PracticeDirection.values();

            // Then
            for (PracticeDirection direction : values) {
                assertThat(direction.name()).contains("FRONT").contains("BACK");
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle enum array operations")
        void shouldHandleEnumArrayOperations() {
            // Given
            PracticeDirection[] values = PracticeDirection.values();

            // When & Then
            assertThat(values).hasSize(2).satisfies(array -> {
                assertThat(array[0]).isEqualTo(PracticeDirection.FRONT_TO_BACK);
                assertThat(array[1]).isEqualTo(PracticeDirection.BACK_TO_FRONT);
            });
        }

        @Test
        @DisplayName("Should handle enum in collections")
        void shouldHandleEnumInCollections() {
            // Given
            List<PracticeDirection> directions =
                    List.of(PracticeDirection.FRONT_TO_BACK, PracticeDirection.BACK_TO_FRONT);

            // When & Then
            assertThat(directions)
                    .hasSize(2)
                    .contains(PracticeDirection.FRONT_TO_BACK)
                    .contains(PracticeDirection.BACK_TO_FRONT);
        }

        @Test
        @DisplayName("Should handle enum in sets")
        void shouldHandleEnumInSets() {
            // Given
            Set<PracticeDirection> directions =
                    Set.of(PracticeDirection.FRONT_TO_BACK, PracticeDirection.BACK_TO_FRONT);

            // When & Then
            assertThat(directions)
                    .hasSize(2)
                    .contains(PracticeDirection.FRONT_TO_BACK)
                    .contains(PracticeDirection.BACK_TO_FRONT);
        }
    }
}
