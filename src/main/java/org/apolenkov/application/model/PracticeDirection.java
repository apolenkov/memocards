package org.apolenkov.application.model;

/**
 * Defines the direction of flashcard practice.
 *
 * <p>This enum represents the two possible directions in which a user can
 * practice flashcards. The direction determines which side of the card
 * is shown first and which side the user needs to recall.</p>
 *
 * <p>Practice direction is a user preference that affects the learning
 * experience and difficulty level of flashcard practice sessions.</p>
 *
 *
 */
public enum PracticeDirection {
    /**
     * Practice from front to back.
     *
     * <p>In this direction, the front side (question/prompt) is shown first,
     * and the user must recall the back side (answer/explanation). This is
     * the traditional flashcard practice method.</p>
     */
    FRONT_TO_BACK,

    /**
     * Practice from back to front.
     *
     * <p>In this direction, the back side (answer/explanation) is shown first,
     * and the user must recall the front side (question/prompt). This reverse
     * practice helps reinforce learning from both directions.</p>
     */
    BACK_TO_FRONT
}
