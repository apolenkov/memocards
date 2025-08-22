package org.apolenkov.application.service;

import org.apolenkov.application.model.PracticeDirection;
import org.springframework.stereotype.Service;

/**
 * Service for managing default practice settings and preferences.
 *
 * <p>This service provides centralized access to default practice configuration
 * values that are used when starting new flashcard practice sessions. It manages
 * settings such as the default number of cards per session, randomization preferences,
 * and practice direction.</p>
 *
 * <p>The service acts as a configuration store that can be modified at runtime
 * to adjust default behavior across the application.</p>
 *
 */
@Service
public class PracticeSettingsService {

    /**
     * Default number of cards to include in a practice session.
     *
     * <p>This value is used when no specific count is provided for a practice session.
     * The value is clamped to ensure it remains within valid bounds (minimum 1).</p>
     */
    private int defaultCount = 10;

    /**
     * Default setting for randomizing card order in practice sessions.
     *
     * <p>When true, cards are presented in random order during practice.
     * When false, cards are presented in their original order.</p>
     */
    private boolean defaultRandomOrder = true;

    /**
     * Default practice direction for flashcard sessions.
     *
     * <p>Determines whether practice sessions start with the front side
     * (question) or back side (answer) of cards by default.</p>
     */
    private PracticeDirection defaultDirection = PracticeDirection.FRONT_TO_BACK;

    /**
     * Gets the default number of cards for practice sessions.
     *
     * @return the default card count for practice sessions
     */
    public int getDefaultCount() {
        return defaultCount;
    }

    /**
     * Sets the default number of cards for practice sessions.
     *
     * <p>The value is automatically clamped to ensure it remains at least 1.
     * This prevents invalid practice session configurations.</p>
     *
     * @param defaultCount the new default card count (minimum 1)
     */
    public void setDefaultCount(int defaultCount) {
        this.defaultCount = Math.clamp(defaultCount, 1, Integer.MAX_VALUE);
    }

    /**
     * Gets the default setting for randomizing card order.
     *
     * @return true if cards are randomized by default, false otherwise
     */
    public boolean isDefaultRandomOrder() {
        return defaultRandomOrder;
    }

    /**
     * Sets the default setting for randomizing card order.
     *
     * @param defaultRandomOrder true to randomize cards by default, false to maintain order
     */
    public void setDefaultRandomOrder(boolean defaultRandomOrder) {
        this.defaultRandomOrder = defaultRandomOrder;
    }

    /**
     * Gets the default practice direction for flashcard sessions.
     *
     * @return the default practice direction
     */
    public PracticeDirection getDefaultDirection() {
        return defaultDirection;
    }

    /**
     * Sets the default practice direction for flashcard sessions.
     *
     * @param defaultDirection the new default practice direction
     */
    public void setDefaultDirection(PracticeDirection defaultDirection) {
        this.defaultDirection = defaultDirection;
    }
}
