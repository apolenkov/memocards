package org.apolenkov.application.views.stats.components;

/**
 * Constants for statistics components.
 * Centralizes all string constants used across stats components to avoid duplication
 * and ensure consistency.
 */
public final class StatsConstants {

    // Translation keys for statistics
    public static final String STATS_SESSIONS = "stats.sessions";
    public static final String STATS_VIEWED = "stats.viewed";
    public static final String STATS_CORRECT = "stats.correct";
    public static final String STATS_HARD = "stats.hard";
    public static final String STATS_COLLAPSE_KEY = "stats.collapse";
    public static final String STATS_EXPAND_KEY = "stats.expand";

    // CSS classes
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String SURFACE_CARD_CLASS = "surface-card";
    public static final String STATS_SECTION_CLASS = "stats-section";
    public static final String STATS_SECTION_TITLE_CLASS = "stats-section__title";
    public static final String CLICKABLE_TITLE_CLASS = "clickable-title";

    // HTML attributes
    public static final String TITLE_ATTRIBUTE = "title";

    // Private constructor to prevent instantiation
    private StatsConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
