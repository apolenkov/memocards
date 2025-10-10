package org.apolenkov.application.views.landing.constants;

import org.apolenkov.application.config.vaadin.VaadinApplicationShell;

/**
 * Constants for landing page components.
 * Centralizes all string constants, CSS class names, and translation keys.
 */
public final class LandingConstants {

    private LandingConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Translation Keys
    public static final String APP_TITLE_KEY = "app.title";
    public static final String LANDING_SUBTITLE_KEY = "landing.subtitle";
    public static final String LANDING_HERO_ALT_KEY = "landing.heroAlt";
    public static final String LANDING_NEWS_KEY = "landing.news";
    public static final String LANDING_GO_TO_DECKS_KEY = "landing.goToDecks";
    public static final String AUTH_LOGIN_KEY = "auth.login";
    public static final String AUTH_REGISTER_KEY = "auth.register";

    // CSS Classes
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String SURFACE_CARD_CLASS = "surface-card";
    public static final String LANDING_HERO_ICON_CLASS = "landing-hero__icon";
    public static final String LANDING_HERO_IMAGE_CLASS = "landing-hero__image";
    public static final String LANDING_HERO_TITLE_CLASS = "landing-hero__title";
    public static final String LANDING_HERO_SUBTITLE_CLASS = "landing-hero__subtitle";
    public static final String LANDING_HERO_SECTION_CLASS = "landing-hero__section";
    public static final String LANDING_NEWS_SECTION_CLASS = "landing-news__section";
    public static final String LANDING_NEWS_TITLE_CLASS = "landing-news__title";
    public static final String LANDING_NEWS_LIST_CLASS = "landing-news__list";
    public static final String LANDING_NEWS_CARD_CLASS = "landing-news__card";
    public static final String LANDING_NEWS_CARD_TITLE_CLASS = "landing-news__card-title";
    public static final String LANDING_NEWS_CARD_CONTENT_CLASS = "landing-news__card-content";
    public static final String LANDING_NEWS_CARD_ACCENT_CLASS = "landing-news__card-accent";

    // Resource Paths
    public static final String PIXEL_ICON_PATH = VaadinApplicationShell.ResourcePaths.PIXEL_ICON_FULL_PATH;
    public static final String PIXEL_ICON_NAME = VaadinApplicationShell.ResourcePaths.PIXEL_ICON_NAME;
}
