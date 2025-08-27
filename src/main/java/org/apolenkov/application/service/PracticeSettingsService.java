package org.apolenkov.application.service;

import org.apolenkov.application.model.PracticeDirection;
import org.springframework.stereotype.Service;

/**
 * Service for managing default practice settings and preferences.
 *
 * <p>Provides centralized access to default practice configuration values used
 * when starting new flashcard practice sessions. Manages settings such as default
 * number of cards per session, randomization preferences, and practice direction.</p>
 */
@Service
public class PracticeSettingsService {

    /**
     * Default number of cards to include in practice session.
     *
     * <p>Used when no specific count is provided for practice session.
     * Value is clamped to ensure it remains within valid bounds (minimum 1).</p>
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
     * <p>Determines whether practice sessions start with front side (question)
     * or back side (answer) of cards by default.</p>
     */
    private PracticeDirection defaultDirection = PracticeDirection.FRONT_TO_BACK;

    /**
     * Gets default number of cards for practice sessions.
     *
     * @return default card count for practice sessions
     */
    public int getDefaultCount() {
        return defaultCount;
    }

    /**
     * Sets default number of cards for practice sessions.
     *
     * <p>Value is automatically clamped to ensure it remains at least 1.
     * Prevents invalid practice session configurations.</p>
     *
     * @param defaultCountValue new default card count (minimum 1)
     */
    public void setDefaultCount(final int defaultCountValue) {
        this.defaultCount = Math.clamp(defaultCountValue, 1, Integer.MAX_VALUE);
    }

    /**
     * Gets default setting for randomizing card order.
     *
     * @return true if cards are randomized by default
     */
    public boolean isDefaultRandomOrder() {
        return defaultRandomOrder;
    }

    /**
     * Sets default setting for randomizing card order.
     *
     * @param defaultRandomOrderValue true to randomize cards by default, false to maintain order
     */
    public void setDefaultRandomOrder(final boolean defaultRandomOrderValue) {
        this.defaultRandomOrder = defaultRandomOrderValue;
    }

    /**
     * Gets default practice direction for flashcard sessions.
     *
     * @return default practice direction
     */
    public PracticeDirection getDefaultDirection() {
        return defaultDirection;
    }

    /**
     * Sets default practice direction for flashcard sessions.
     *
     * @param defaultDirectionValue new default practice direction
     */
    public void setDefaultDirection(final PracticeDirection defaultDirectionValue) {
        this.defaultDirection = defaultDirectionValue;
    }
}
