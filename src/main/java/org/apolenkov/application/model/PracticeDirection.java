package org.apolenkov.application.model;

/**
 * Defines the direction of card practice.
 *
 * <p>Represents the two possible directions in which a user can practice cards.
 * Direction determines which side of the card is shown first and which side
 * the user needs to recall.</p>
 */
public enum PracticeDirection {
    /**
     * Practice from front to back.
     *
     * <p>Front side (question/prompt) is shown first, user must recall
     * back side (answer/explanation). Traditional card practice method.</p>
     */
    FRONT_TO_BACK,

    /**
     * Practice from back to front.
     *
     * <p>Back side (answer/explanation) is shown first, user must recall
     * front side (question/prompt). Reverse practice helps reinforce learning
     * from both directions.</p>
     */
    BACK_TO_FRONT
}
