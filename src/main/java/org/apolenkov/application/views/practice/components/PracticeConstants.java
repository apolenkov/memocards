package org.apolenkov.application.views.practice.components;

/**
 * Constants for the practice module.
 * Centralizes all string constants, CSS class names, HTML attributes, and translation keys
 * used across the practice module components.
 */
public final class PracticeConstants {

    // Translation Keys
    public static final String PRACTICE_TITLE_KEY = "practice.title";
    public static final String PRACTICE_BACK_KEY = "practice.back";
    public static final String PRACTICE_GET_READY_KEY = "practice.getReady";
    public static final String PRACTICE_LOADING_CARDS_KEY = "practice.loadingCards";
    public static final String PRACTICE_SHOW_ANSWER_KEY = "practice.showAnswer";
    public static final String PRACTICE_KNOW_KEY = "practice.know";
    public static final String PRACTICE_HARD_KEY = "practice.hard";
    public static final String PRACTICE_ALL_KNOWN_KEY = "practice.allKnown";
    public static final String PRACTICE_PROGRESS_LINE_KEY = "practice.progressLine";
    public static final String PRACTICE_EXAMPLE_PREFIX_KEY = "practice.example.prefix";
    public static final String PRACTICE_SESSION_COMPLETE_KEY = "practice.sessionComplete";
    public static final String PRACTICE_RESULTS_KEY = "practice.results";
    public static final String PRACTICE_TIME_KEY = "practice.time";
    public static final String PRACTICE_REPEAT_HARD_KEY = "practice.repeatHard";
    public static final String PRACTICE_BACK_TO_DECK_KEY = "practice.backToDeck";
    public static final String PRACTICE_BACK_TO_DECKS_KEY = "practice.backToDecks";
    public static final String PRACTICE_INVALID_ID_KEY = "practice.invalidId";
    public static final String DECK_NOT_FOUND_KEY = "deck.notFound";
    public static final String COMMON_BACK_KEY = "common.back";

    // Settings Translation Keys
    public static final String SETTINGS_TITLE_KEY = "settings.title";
    public static final String SETTINGS_COUNT_KEY = "settings.count";
    public static final String SETTINGS_MODE_KEY = "settings.mode";
    public static final String SETTINGS_MODE_RANDOM_KEY = "settings.mode.random";
    public static final String SETTINGS_MODE_SEQUENTIAL_KEY = "settings.mode.sequential";
    public static final String SETTINGS_DIRECTION_KEY = "settings.direction";
    public static final String SETTINGS_DIRECTION_F2B_KEY = "settings.direction.f2b";
    public static final String SETTINGS_DIRECTION_B2F_KEY = "settings.direction.b2f";
    public static final String SETTINGS_SAVE_KEY = "settings.save";
    public static final String SETTINGS_SAVED_KEY = "settings.saved";
    public static final String COMMON_CANCEL_KEY = "common.cancel";

    // CSS Classes
    public static final String CONTAINER_MD_CLASS = "container-md";
    public static final String PRACTICE_VIEW_SECTION_CLASS = "practice-view__section";
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String PRACTICE_VIEW_DECK_TITLE_CLASS = "practice-view__deck-title";
    public static final String PRACTICE_PROGRESS_CLASS = "practice-progress";
    public static final String PRACTICE_PROGRESS_TEXT_CLASS = "practice-progress__text";
    public static final String PRACTICE_CARD_CONTAINER_CLASS = "practice-card-container";
    public static final String PRACTICE_CARD_CONTENT_CLASS = "practice-card-content";
    public static final String DIALOG_MD_CLASS = "dialog-md";

    // Button Variants
    public static final String KNOW_LABEL = "know";
    public static final String HARD_LABEL = "hard";

    // Private constructor to prevent instantiation
    private PracticeConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
