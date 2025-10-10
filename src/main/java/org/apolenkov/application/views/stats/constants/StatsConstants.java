package org.apolenkov.application.views.stats.constants;

/**
 * Constants used across statistics components.
 * Centralizes all string constants to avoid duplication and ensure consistency.
 */
public final class StatsConstants {

    // CSS Classes
    public static final String SURFACE_CARD_CLASS = "surface-card";
    public static final String STATS_CARD_CLASS = "stats-card";
    public static final String STATS_CARD_VALUE_CLASS = "stats-card__value";
    public static final String STATS_CARD_LABEL_CLASS = "stats-card__label";

    public static final String DECK_STATS_CARD_CLASS = "deck-stats-card";
    public static final String DECK_STATS_CARD_HEADER_CLASS = "deck-stats-card__header";
    public static final String DECK_STATS_CARD_TITLE_CLASS = "deck-stats-card__title";
    public static final String STATS_DECK_GRID_CLASS = "stats-deck-grid";

    public static final String STATS_DECK_ITEM_CLASS = "stats-deck-item";
    public static final String STATS_DECK_ITEM_TOTAL_CLASS = "stats-deck-item__total";
    public static final String STATS_DECK_ITEM_TODAY_CLASS = "stats-deck-item__today";
    public static final String STATS_DECK_ITEM_LABEL_CLASS = "stats-deck-item__label";

    public static final String STATS_SECTION_CLASS = "stats-section";
    public static final String STATS_SECTION_HEADER_CLASS = "stats-section__header";
    public static final String STATS_SECTION_TITLE_CLASS = "stats-section__title";
    public static final String CLICKABLE_TITLE_CLASS = "clickable-title";

    public static final String STATS_OVERALL_GRID_CLASS = "stats-overall-grid";
    public static final String STATS_TODAY_GRID_CLASS = "stats-today-grid";

    public static final String STATS_PAGINATION_INDICATOR_CLASS = "stats-pagination__indicator";
    public static final String STATS_CURRENT_DECK_CONTAINER_CLASS = "stats-current-deck__container";

    public static final String STATS_VIEW_CLASS = "stats-view";
    public static final String STATS_VIEW_TITLE_CLASS = "stats-view__title";
    public static final String STATS_PAGE_SECTION_CLASS = "stats-page__section";
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String CONTAINER_MD_CLASS = "container-md";

    // HTML Attributes
    public static final String TITLE_ATTRIBUTE = "title";

    // Translation Keys
    public static final String STATS_TITLE_KEY = "stats.title";
    public static final String STATS_TODAY_KEY = "stats.today";
    public static final String STATS_OVERALL_KEY = "stats.overall";
    public static final String STATS_BY_DECK_KEY = "stats.byDeck";
    public static final String STATS_NO_DECKS_KEY = "stats.noDecks";

    public static final String STATS_SESSIONS_KEY = "stats.sessions";
    public static final String STATS_VIEWED_KEY = "stats.viewed";
    public static final String STATS_CORRECT_KEY = "stats.correct";
    public static final String STATS_HARD_KEY = "stats.hard";

    public static final String STATS_COLLAPSE_KEY = "stats.collapse";
    public static final String STATS_EXPAND_KEY = "stats.expand";
    public static final String STATS_PREVIOUS_DECK_KEY = "stats.previousDeck";
    public static final String STATS_NEXT_DECK_KEY = "stats.nextDeck";
    public static final String STATS_DECK_PAGE_KEY = "stats.deckPage";

    // Private constructor to prevent instantiation
    private StatsConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
