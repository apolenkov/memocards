package org.apolenkov.application.views.auth.constants;

/**
 * Centralized constants for the auth module.
 * Contains CSS classes, translation keys, and other hardcoded values.
 */
public final class AuthConstants {

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

    // Validation keys
    public static final String VALIDATION_NAME_REQUIRED_KEY = "auth.validation.nameRequired";
    public static final String VALIDATION_NAME_MIN2_KEY = "auth.validation.nameMin2";
    public static final String VALIDATION_EMAIL_REQUIRED_KEY = "auth.validation.emailRequired";
    public static final String VALIDATION_EMAIL_INVALID_KEY = "auth.validation.invalidEmail";
    public static final String VALIDATION_PASSWORD_POLICY_KEY = "auth.validation.passwordPolicy";
    public static final String VALIDATION_PASSWORDS_MISMATCH_KEY = "auth.validation.passwordsMismatch";
    public static final String VALIDATION_FIX_ERRORS_KEY = "auth.validation.fixErrors";

    // Vaadin validation keys
    public static final String VAADIN_VALIDATION_EMAIL_REQUIRED_KEY = "vaadin.validation.email.required";
    public static final String VAADIN_VALIDATION_PASSWORD_REQUIRED_KEY = "vaadin.validation.password.required";

    // Auth page keys
    public static final String AUTH_LOGIN_KEY = "auth.login";
    public static final String AUTH_LOGIN_SUBMIT_KEY = "auth.login.submit";
    public static final String AUTH_LOGIN_PASSWORD_KEY = "auth.login.password";
    public static final String AUTH_LOGIN_ERROR_MESSAGE_KEY = "auth.login.errorMessage";
    public static final String AUTH_LOGIN_FORGOT_PASSWORD_KEY = "auth.login.forgotPassword";

    public static final String AUTH_REGISTER_KEY = "auth.register";
    public static final String AUTH_REGISTER_TITLE_KEY = "auth.register.title";
    public static final String AUTH_REGISTER_SUCCESS_KEY = "auth.register.successLogin";
    public static final String AUTH_REGISTER_ERROR_KEY = "auth.register.error";

    public static final String AUTH_EMAIL_KEY = "auth.email";
    public static final String AUTH_EMAIL_PLACEHOLDER_KEY = "auth.email.placeholder";
    public static final String AUTH_NAME_KEY = "auth.name";
    public static final String AUTH_NAME_PLACEHOLDER_KEY = "auth.name.placeholder";
    public static final String AUTH_PASSWORD_KEY = "auth.password";
    public static final String AUTH_PASSWORD_PLACEHOLDER_KEY = "auth.password.placeholder";
    public static final String AUTH_PASSWORD_CONFIRM_KEY = "auth.password.confirm";
    public static final String AUTH_PASSWORD_CONFIRM_PLACEHOLDER_KEY = "auth.password.confirm.placeholder";

    // Forgot password keys
    public static final String AUTH_FORGOT_PASSWORD_TITLE_KEY = "auth.forgotPassword.title";
    public static final String AUTH_FORGOT_PASSWORD_SUBMIT_KEY = "auth.forgotPassword.submit";
    public static final String AUTH_FORGOT_PASSWORD_BACK_TO_LOGIN_KEY = "auth.forgotPassword.backToLogin";
    public static final String AUTH_FORGOT_PASSWORD_EMAIL_REQUIRED_KEY = "auth.forgotPassword.emailRequired";
    public static final String AUTH_FORGOT_PASSWORD_TOKEN_CREATED_KEY = "auth.forgotPassword.tokenCreated";
    public static final String AUTH_FORGOT_PASSWORD_EMAIL_NOT_FOUND_KEY = "auth.forgotPassword.emailNotFound";
    public static final String AUTH_FORGOT_PASSWORD_ERROR_KEY = "auth.forgotPassword.error";

    // Reset password keys
    public static final String AUTH_RESET_PASSWORD_TITLE_KEY = "auth.resetPassword.title";
    public static final String AUTH_RESET_PASSWORD_SUBMIT_KEY = "auth.resetPassword.submit";
    public static final String AUTH_RESET_PASSWORD_BACK_TO_LOGIN_KEY = "auth.resetPassword.backToLogin";
    public static final String AUTH_RESET_PASSWORD_INVALID_TOKEN_KEY = "auth.resetPassword.invalidToken";
    public static final String AUTH_RESET_PASSWORD_PASSWORD_REQUIRED_KEY = "auth.resetPassword.passwordRequired";
    public static final String AUTH_RESET_PASSWORD_PASSWORD_MISMATCH_KEY = "auth.resetPassword.passwordMismatch";
    public static final String AUTH_RESET_PASSWORD_PASSWORD_TOO_SHORT_KEY = "auth.resetPassword.passwordTooShort";
    public static final String AUTH_RESET_PASSWORD_SUCCESS_KEY = "auth.resetPassword.success";
    public static final String AUTH_RESET_PASSWORD_FAILED_KEY = "auth.resetPassword.failed";
    public static final String AUTH_RESET_PASSWORD_ERROR_KEY = "auth.resetPassword.error";

    // Common keys
    public static final String COMMON_BACK_TO_HOME_KEY = "common.backToHome";

    private AuthConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
