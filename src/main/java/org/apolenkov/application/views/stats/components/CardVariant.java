package org.apolenkov.application.views.stats.components;

/**
 * Enumeration for different visual variants of statistics cards.
 * Each variant corresponds to a semantic meaning and visual styling.
 */
public enum CardVariant {
    /**
     * Primary variant - used for main metrics like sessions.
     * Styled with primary color accent.
     */
    PRIMARY,

    /**
     * Success variant - used for positive metrics like correct answers.
     * Styled with success color accent.
     */
    SUCCESS,

    /**
     * Warning variant - used for metrics requiring attention like hard cards.
     * Styled with warning/error color accent.
     */
    WARNING,

    /**
     * Info variant - used for informational metrics like viewed cards.
     * Styled with neutral color accent.
     */
    INFO
}
