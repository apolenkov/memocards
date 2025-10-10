package org.apolenkov.application.views.core.constants;

/**
 * Constants for core module components.
 * Centralizes all string constants used across the core module.
 */
public final class CoreConstants {

    // CSS Classes
    public static final String ERROR_CONTAINER_CLASS = "error-container";
    public static final String ERROR_VIEW_TITLE_CLASS = "error-view__title";
    public static final String ERROR_VIEW_DESCRIPTION_CLASS = "error-view__description";

    public static final String ERROR_DEV_CONTAINER_CLASS = "error-dev__container";
    public static final String ERROR_DEV_TITLE_CLASS = "error-dev__title";
    public static final String ERROR_DEV_DETAILS_CLASS = "error-dev__details";
    public static final String ERROR_DEV_TYPE_CLASS = "error-dev__type";
    public static final String ERROR_DEV_MESSAGE_CLASS = "error-dev__message";
    public static final String ERROR_DEV_ROUTE_CLASS = "error-dev__route";
    public static final String ERROR_DEV_TIMESTAMP_CLASS = "error-dev__timestamp";
    public static final String ERROR_DEV_ID_CLASS = "error-dev__id";

    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String CONTAINER_MD_CLASS = "container-md";

    public static final String ENTITY_ERROR_SECTION_CLASS = "entity-error__section";
    public static final String ENTITY_ERROR_TITLE_CLASS = "entity-error__title";
    public static final String ENTITY_ERROR_DESCRIPTION_CLASS = "entity-error__description";
    public static final String ENTITY_ERROR_SUGGESTION_CLASS = "entity-error__suggestion";

    public static final String NOT_FOUND_CONTAINER_CLASS = "not-found-container";
    public static final String NOT_FOUND_CODE_CLASS = "not-found__code";
    public static final String NOT_FOUND_TITLE_CLASS = "not-found__title";
    public static final String NOT_FOUND_DESCRIPTION_CLASS = "not-found__description";
    public static final String NOT_FOUND_SUGGESTION_CLASS = "not-found__suggestion";
    public static final String NOT_FOUND_GO_HOME_SUGGESTION_CLASS = "not-found__go-home-suggestion";

    public static final String TEXT_CENTER_CLASS = "text-center";

    public static final String LANGUAGE_SWITCHER_LABEL_CLASS = "language-switcher__label";
    public static final String LANGUAGE_SWITCHER_COMBO_CLASS = "language-switcher__combo";

    public static final String DIALOG_SM_CLASS = "dialog-sm";

    public static final String TOP_MENU_GREETING_CLASS = "top-menu__greeting";

    public static final String MAIN_LAYOUT_NAVBAR_CLASS = "main-layout__navbar";
    public static final String MAIN_LAYOUT_RIGHT_CLASS = "main-layout__right";

    public static final String PUBLIC_LAYOUT_CLASS = "public-layout";
    public static final String APP_CONTENT_CLASS = "app-content";

    // Translation Keys
    public static final String ERROR_500_KEY = "error.500";
    public static final String ERROR_500_DESCRIPTION_KEY = "error.500.description";

    public static final String ERROR_DEV_TITLE_KEY = "error.dev.title";
    public static final String ERROR_TIMESTAMP_KEY = "error.timestamp";
    public static final String ERROR_ID_KEY = "error.id";
    public static final String ERROR_TYPE_KEY = "error.type";
    public static final String ERROR_MESSAGE_KEY = "error.message";
    public static final String ERROR_CURRENT_ROUTE_KEY = "error.current.route";
    public static final String ERROR_UNKNOWN_KEY = "error.unknown";

    public static final String ENTITY_NOT_FOUND_TITLE_KEY = "entity.notFound.title";
    public static final String ENTITY_NOT_FOUND_DESCRIPTION_KEY = "entity.notFound.description";
    public static final String ENTITY_NOT_FOUND_SUGGESTION_KEY = "entity.notFound.suggestion";
    public static final String ENTITY_NOT_FOUND_GO_BACK_KEY = "entity.notFound.goBack";

    public static final String MAIN_GO_HOME_KEY = "main.gohome";

    public static final String ERROR_404_KEY = "error.404";
    public static final String ERROR_404_TITLE_KEY = "error.404.title";
    public static final String ERROR_404_DESCRIPTION_KEY = "error.404.description";
    public static final String ERROR_404_SUGGESTION_KEY = "error.404.suggestion";
    public static final String ERROR_404_GO_HOME_KEY = "error.404.goHome";

    public static final String ERROR_TRY_AGAIN_KEY = "error.tryAgain";
    public static final String ERROR_GO_HOME_KEY = "error.goHome";
    public static final String COMMON_BACK_KEY = "common.back";

    public static final String LANGUAGE_LABEL_KEY = "language.label";
    public static final String LANGUAGE_EN_KEY = "language.en";
    public static final String LANGUAGE_RU_KEY = "language.ru";
    public static final String LANGUAGE_ES_KEY = "language.es";

    public static final String AUTH_LOGOUT_CONFIRM_KEY = "auth.logout.confirm";
    public static final String DIALOG_CONFIRM_KEY = "dialog.confirm";
    public static final String DIALOG_CANCEL_KEY = "dialog.cancel";

    public static final String MAIN_GREETING_KEY = "main.greeting";
    public static final String APP_TITLE_KEY = "app.title";

    // Navigation Translation Keys
    public static final String MAIN_DECKS_KEY = "main.decks";
    public static final String MAIN_STATS_KEY = "main.stats";
    public static final String MAIN_SETTINGS_KEY = "main.settings";
    public static final String MAIN_LOGOUT_KEY = "main.logout";
    public static final String ADMIN_CONTENT_TITLE_KEY = "admin.content.page.title";

    // Query Parameter Names
    public static final String FROM_PARAM = "from";
    public static final String ERROR_PARAM = "error";
    public static final String MESSAGE_PARAM = "message";
    public static final String ID_PARAM = "id";

    // HTML Attributes
    public static final String DATA_TEST_ID_ATTRIBUTE = "data-test-id";
    public static final String ARIA_LABEL_ATTRIBUTE = "aria-label";

    // Locale Codes
    public static final String RU_LOCALE = "ru";
    public static final String ES_LOCALE = "es";
    public static final String EN_LOCALE = "en";

    // Patterns and Limits
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final int MAX_ERROR_DETAIL_LENGTH = 200;
    public static final String TRUNCATION_SUFFIX = "...";

    // Profiles
    public static final String DEV_PROFILE = "dev";

    // Text Separators
    public static final String SEPARATOR_COLON_SPACE = ": ";
    public static final String SEPARATOR_SPACE = " ";

    // Template Placeholders
    public static final String PLACEHOLDER_0 = "{0}";

    // Test IDs for navigation buttons
    public static final String NAV_DECKS_TEST_ID = "nav-decks";
    public static final String NAV_STATS_TEST_ID = "nav-stats";
    public static final String NAV_SETTINGS_TEST_ID = "nav-settings";
    public static final String NAV_ADMIN_CONTENT_TEST_ID = "nav-admin-content";
    public static final String NAV_LOGOUT_TEST_ID = "nav-logout";

    // Private constructor to prevent instantiation
    private CoreConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
