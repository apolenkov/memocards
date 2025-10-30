package org.apolenkov.application.views.practice.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages UI state for practice components.
 * Uses @UIScope to maintain state per browser tab.
 */
@org.springframework.stereotype.Component
@UIScope
public class PracticeUIStateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PracticeUIStateManager.class);

    // ==================== Fields ====================

    private PracticeActions practiceActions;
    private PracticeCard practiceCard;
    private PracticeDisplay practiceDisplay;
    private PracticeCongratulations practiceCongratulations;

    // ==================== Constructor ====================

    /**
     * Creates PracticeUIStateManager with required dependencies.
     */
    public PracticeUIStateManager() {
        LOGGER.debug("PracticeUIStateManager created");
    }

    // ==================== Component Management ====================

    /**
     * Registers a practice component for state management.
     *
     * @param component the component to register
     * @throws IllegalArgumentException if component type is not supported
     */
    public void registerComponent(final Component component) {
        switch (component) {
            case PracticeActions actions -> this.practiceActions = actions;
            case PracticeCard card -> this.practiceCard = card;
            case PracticeDisplay display -> this.practiceDisplay = display;
            case PracticeCongratulations congratulations -> this.practiceCongratulations = congratulations;
            default -> throw new IllegalArgumentException("Unsupported component type: " + component.getClass());
        }

        LOGGER.debug("Registered component: {}", component.getClass().getSimpleName());
    }

    /**
     * Resets UI components to their default visible state.
     * This ensures proper state when returning to practice after adding cards.
     */
    public void resetToPracticeState() {
        // Show all practice components
        setComponentVisible(practiceActions, true);
        setComponentVisible(practiceCard, true);
        setComponentVisible(practiceDisplay, true);

        if (practiceDisplay != null) {
            practiceDisplay.showProgress();
        }

        // Hide congratulations component
        setComponentVisible(practiceCongratulations, false);

        LOGGER.debug("UI state reset to practice mode");
    }

    /**
     * Sets UI to congratulations state.
     * Hides practice components and shows congratulations.
     */
    public void setToCongratulationsState() {
        // Hide all practice components completely
        setComponentVisible(practiceActions, false);
        setComponentVisible(practiceCard, false);
        setComponentVisible(practiceDisplay, false);

        // Show congratulations component
        setComponentVisible(practiceCongratulations, true);

        LOGGER.debug("UI state set to congratulations mode");
    }

    /**
     * Checks if all required components are registered.
     *
     * @return true if all components are registered
     */
    public boolean isFullyInitialized() {
        return practiceActions != null
                && practiceCard != null
                && practiceDisplay != null
                && practiceCongratulations != null;
    }

    // ==================== Private Methods ====================

    /**
     * Safely sets component visibility.
     *
     * @param component the component to set visibility for
     * @param visible the visibility state
     */
    private void setComponentVisible(final Component component, final boolean visible) {
        if (component != null) {
            component.setVisible(visible);
        } else {
            LOGGER.debug("Component is null, skipping visibility change");
        }
    }
}
