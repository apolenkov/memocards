package org.apolenkov.application.views.auth.constants;

/**
 * Centralized constants for the auth module.
 * Contains CSS classes, translation keys, and other hardcoded values.
 */
public final class AuthConstants {

    private AuthConstants() {}

    // CSS Classes
    public static final String AUTH_FORM_CLASS = "auth-form";
    public static final String SURFACE_PANEL_CLASS = "surface-panel";
    public static final String FORM_SPACING_CLASS = "form-spacing";

    // Form-specific CSS classes
    public static final String LOGIN_FORM_CLASS = "login-form";
    public static final String LOGIN_FORM_TITLE_CONTAINER_CLASS = "login-form__title-container";
    public static final String LOGIN_FORM_TITLE_CLASS = "login-form__title";

    public static final String REGISTER_FORM_CLASS = "register-form";
    public static final String REGISTER_FORM_TITLE_CONTAINER_CLASS = "register-form__title-container";
    public static final String REGISTER_FORM_TITLE_CLASS = "register-form__title";

    public static final String FORGOT_PASSWORD_FORM_CLASS = "forgot-password-form";
    public static final String FORGOT_PASSWORD_FORM_TITLE_CLASS = "forgot-password-form__title";

    public static final String RESET_PASSWORD_FORM_CLASS = "reset-password-form";
    public static final String RESET_PASSWORD_FORM_TITLE_CLASS = "reset-password-form__title";
}
