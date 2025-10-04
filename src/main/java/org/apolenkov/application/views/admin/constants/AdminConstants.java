package org.apolenkov.application.views.admin.constants;

/**
 * Centralized constants for the admin module.
 * Contains CSS classes, translation keys, and other hardcoded values.
 */
public final class AdminConstants {

    private AdminConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // CSS Classes
    public static final String ADMIN_CONTENT_VIEW_CLASS = "admin-content-view";
    public static final String ADMIN_CONTENT_VIEW_CONTENT_CLASS = "admin-content-view__content";
    public static final String ADMIN_CONTENT_VIEW_TITLE_CLASS = "admin-content-view__title";
    public static final String ADMIN_CONTENT_TOOLBAR_CLASS = "admin-content-toolbar";
    public static final String ADMIN_CONTENT_SECTION_CLASS = "admin-content-section";
    public static final String ADMIN_CONTENT_EMPTY_MESSAGE_CLASS = "admin-content-empty-message";
    public static final String ADMIN_NEWS_CONTENT_AREA_CLASS = "admin-news__content-area";
    public static final String NEWS_CARD_CLASS = "news-card";
    public static final String NEWS_CARD_TITLE_CLASS = "news-card__title";
    public static final String NEWS_CARD_CONTENT_CLASS = "news-card__content";
    public static final String NEWS_CARD_AUTHOR_CLASS = "news-card__author";
    public static final String NEWS_CARD_DATE_CLASS = "news-card__date";
    public static final String TEXT_CENTER_CLASS = "text-center";

    // Common Classes
    public static final String CONTAINER_MD_CLASS = "container-md";
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String DIALOG_MD_CLASS = "dialog-md";
    public static final String DIALOG_SM_CLASS = "dialog-sm";

    // Note: Inline styles moved to CSS classes in admin-news-view.css

    // CSS Utility Classes
    public static final String ADMIN_DIALOG_CONFIRM_TITLE_CLASS = "admin-dialog-confirm__title";
    public static final String TEXT_CONTENT_CLASS = "text-content";
    public static final String TEXT_MUTED_CLASS = "text-muted";
    public static final String TEXT_MUTED_SMALL_CLASS = "text-muted-small";

    // Content Preview
    public static final int CONTENT_PREVIEW_LENGTH = 150;
    public static final String CONTENT_PREVIEW_SUFFIX = "...";

    // Date Format
    public static final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm";

    // Translation Keys
    public static final String ADMIN_CONTENT_PAGE_TITLE_KEY = "admin.content.page.title";
    public static final String ADMIN_CONTENT_SEARCH_PLACEHOLDER_KEY = "admin.content.search.placeholder";
    public static final String ADMIN_CONTENT_SEARCH_NO_RESULTS_KEY = "admin.content.search.noResults";
    public static final String ADMIN_NEWS_ADD_KEY = "admin.news.add";
    public static final String ADMIN_NEWS_TITLE_KEY = "admin.news.title";
    public static final String ADMIN_NEWS_CONTENT_KEY = "admin.news.content";
    public static final String ADMIN_NEWS_AUTHOR_KEY = "admin.news.author";
    public static final String ADMIN_NEWS_DELETED_KEY = "admin.news.deleted";
    public static final String ADMIN_NEWS_ERROR_CREATE_KEY = "admin.news.error.create";
    public static final String ADMIN_NEWS_ERROR_UPDATE_KEY = "admin.news.error.update";
    public static final String ADMIN_NEWS_ERROR_DELETE_KEY = "admin.news.error.delete";
    public static final String ADMIN_NEWS_CREATED_KEY = "admin.news.created";
    public static final String ADMIN_NEWS_UPDATED_KEY = "admin.news.updated";
    public static final String ADMIN_NEWS_CONFIRM_DELETE_TITLE_KEY = "admin.news.confirm.delete.title";
    public static final String ADMIN_NEWS_CONFIRM_DELETE_PREFIX_KEY = "admin.news.confirm.delete.prefix";
    public static final String ADMIN_NEWS_CONFIRM_DELETE_SUFFIX_KEY = "admin.news.confirm.delete.suffix";
}
