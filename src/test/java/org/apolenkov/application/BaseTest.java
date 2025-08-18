package org.apolenkov.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Base test class providing common testing utilities and patterns.
 * Eliminates duplication of test structure across test classes.
 */
public abstract class BaseTest {

    protected final LocalDateTime testTime = LocalDateTime.of(2024, 1, 1, 12, 0);

    @BeforeEach
    abstract void setUp();

    /**
     * Assert that a timestamp is close to current time
     */
    protected void assertTimestampCloseToNow(LocalDateTime timestamp) {
        assertThat(timestamp).isNotNull();
        assertThat(timestamp).isAfter(LocalDateTime.now().minusSeconds(5));
        assertThat(timestamp).isBefore(LocalDateTime.now().plusSeconds(5));
    }

    /**
     * Assert that a required field validation throws exception for null/empty values
     */
    protected void assertRequiredFieldValidation(String value, String expectedMessage) {
        assertThatThrownBy(() -> validateRequiredField(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    /**
     * Assert that a required field validation throws exception for null values
     */
    protected void assertRequiredFieldValidation(Object value, String expectedMessage) {
        assertThatThrownBy(() -> validateRequiredField(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expectedMessage);
    }

    /**
     * Assert that trimming works correctly
     */
    protected void assertTrimmingWorks(String input, String expected) {
        String result = trimField(input);
        assertThat(result).isEqualTo(expected);
    }

    /**
     * Abstract methods to be implemented by subclasses
     */
    protected abstract void validateRequiredField(String value);

    protected abstract void validateRequiredField(Object value);

    protected abstract String trimField(String value);

    /**
     * Common test structure for entity constructors
     */
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should initialize with current time")
        void defaultConstructorShouldInitializeWithCurrentTime() {
            testDefaultConstructor();
        }

        @Test
        @DisplayName("Parameterized constructor should set all fields")
        void parameterizedConstructorShouldSetAllFields() {
            testParameterizedConstructor();
        }
    }

    /**
     * Common test structure for validation
     */
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw exception for null or empty values")
        void shouldThrowExceptionForNullOrEmptyValues(String value) {
            testValidationForNullOrEmpty(value);
        }

        @Test
        @DisplayName("Should handle null optional values")
        void shouldHandleNullOptionalValues() {
            testNullOptionalValues();
        }
    }

    /**
     * Abstract test methods to be implemented by subclasses
     */
    protected abstract void testDefaultConstructor();

    protected abstract void testParameterizedConstructor();

    protected abstract void testValidationForNullOrEmpty(String value);

    protected abstract void testNullOptionalValues();
}
