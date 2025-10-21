package org.apolenkov.application.views.practice.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for PracticeUIStateManager component lifecycle and state management.
 * Verifies proper @UIScope behavior and component registration.
 */
@ExtendWith(MockitoExtension.class)
class PracticeUIStateManagerTest {

    private PracticeUIStateManager stateManager;

    @Mock
    private PracticeActions practiceActions;

    @Mock
    private PracticeCard practiceCard;

    @Mock
    private PracticeDisplay practiceDisplay;

    @Mock
    private PracticeCongratulations practiceCongratulations;

    @BeforeEach
    void setUp() {
        stateManager = new PracticeUIStateManager();
    }

    @Test
    @DisplayName("Should register PracticeActions component")
    void shouldRegisterPracticeActions() {
        // When
        stateManager.registerComponent(practiceActions);

        // Then
        assertThat(stateManager.isFullyInitialized()).isFalse(); // Not all components registered yet
    }

    @Test
    @DisplayName("Should register PracticeCard component")
    void shouldRegisterPracticeCard() {
        // When
        stateManager.registerComponent(practiceCard);

        // Then
        assertThat(stateManager.isFullyInitialized()).isFalse(); // Not all components registered yet
    }

    @Test
    @DisplayName("Should register PracticeDisplay component")
    void shouldRegisterPracticeDisplay() {
        // When
        stateManager.registerComponent(practiceDisplay);

        // Then
        assertThat(stateManager.isFullyInitialized()).isFalse(); // Not all components registered yet
    }

    @Test
    @DisplayName("Should register PracticeCongratulations component")
    void shouldRegisterPracticeCongratulations() {
        // When
        stateManager.registerComponent(practiceCongratulations);

        // Then
        assertThat(stateManager.isFullyInitialized()).isFalse(); // Not all components registered yet
    }

    @Test
    @DisplayName("Should be fully initialized when all components are registered")
    void shouldBeFullyInitializedWhenAllComponentsRegistered() {
        // When
        stateManager.registerComponent(practiceActions);
        stateManager.registerComponent(practiceCard);
        stateManager.registerComponent(practiceDisplay);
        stateManager.registerComponent(practiceCongratulations);

        // Then
        assertThat(stateManager.isFullyInitialized()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for unsupported component type")
    void shouldThrowExceptionForUnsupportedComponent() {
        // Given - use Div as an unsupported component type (not PracticeActions/Card/Display/Congratulations)
        Component unsupportedComponent = new Div();

        // When & Then
        assertThatThrownBy(() -> stateManager.registerComponent(unsupportedComponent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported component type");
    }

    @Test
    @DisplayName("Should handle null components gracefully")
    void shouldHandleNullComponentsGracefully() {
        // When & Then - should not throw exception
        stateManager.resetToPracticeState();
        stateManager.setToCongratulationsState();

        // Verify no exceptions thrown
        assertThat(stateManager.isFullyInitialized()).isFalse();
    }

    @Test
    @DisplayName("Should reset to practice state")
    void shouldResetToPracticeState() {
        // Given
        stateManager.registerComponent(practiceActions);
        stateManager.registerComponent(practiceCard);
        stateManager.registerComponent(practiceDisplay);
        stateManager.registerComponent(practiceCongratulations);

        // When
        stateManager.resetToPracticeState();

        // Then - should not throw exception
        assertThat(stateManager.isFullyInitialized()).isTrue();
    }

    @Test
    @DisplayName("Should set to congratulations state")
    void shouldSetToCongratulationsState() {
        // Given
        stateManager.registerComponent(practiceActions);
        stateManager.registerComponent(practiceCard);
        stateManager.registerComponent(practiceDisplay);
        stateManager.registerComponent(practiceCongratulations);

        // When
        stateManager.setToCongratulationsState();

        // Then - should not throw exception
        assertThat(stateManager.isFullyInitialized()).isTrue();
    }
}
