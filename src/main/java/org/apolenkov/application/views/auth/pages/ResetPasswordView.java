package org.apolenkov.application.views.auth.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.service.security.PasswordResetService;
import org.apolenkov.application.views.auth.constants.AuthConstants;
import org.apolenkov.application.views.auth.utils.PasswordValidator;
import org.apolenkov.application.views.core.exception.EntityNotFoundException;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.AuthRedirectHelper;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

/**
 * Password reset form view for completing password reset process.
 * Validates reset tokens and allows users to set new passwords.
 * Accessible only to anonymous users with valid reset tokens.
 */
@Route(value = RouteConstants.RESET_PASSWORD_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public class ResetPasswordView extends BaseView implements HasUrlParameter<String>, BeforeEnterObserver {

    private final transient PasswordResetService passwordResetService;
    private String token;
    /**
     * Creates a new ResetPasswordView with password reset service dependency.
     *
     * @param service service for handling password reset operations
     */
    public ResetPasswordView(final PasswordResetService service) {
        this.passwordResetService = service;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        VerticalLayout wrapper = createCenteredVerticalLayout();

        // Create a beautiful Lumo-styled form container
        Div formContainer = new Div();
        formContainer.addClassName(AuthConstants.RESET_PASSWORD_FORM_CLASS);
        formContainer.addClassName(AuthConstants.AUTH_FORM_CLASS);
        formContainer.addClassName(AuthConstants.SURFACE_PANEL_CLASS);

        // Create form title
        H2 title = new H2(getTranslation(AuthConstants.AUTH_RESET_PASSWORD_TITLE_KEY));
        title.addClassName(AuthConstants.RESET_PASSWORD_FORM_TITLE_CLASS);

        // Create form fields container
        VerticalLayout formFields = new VerticalLayout();
        formFields.setSpacing(true);
        formFields.setAlignItems(FlexComponent.Alignment.CENTER);

        PasswordField password = new PasswordField(getTranslation(AuthConstants.AUTH_PASSWORD_KEY));
        password.setPlaceholder(getTranslation(AuthConstants.AUTH_PASSWORD_PLACEHOLDER_KEY));
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);

        PasswordField confirmPassword = new PasswordField(getTranslation(AuthConstants.AUTH_PASSWORD_CONFIRM_KEY));
        confirmPassword.setPlaceholder(getTranslation(AuthConstants.AUTH_PASSWORD_CONFIRM_PLACEHOLDER_KEY));
        confirmPassword.setWidthFull();
        confirmPassword.setRequiredIndicatorVisible(true);

        Button submit = ButtonHelper.createPrimaryButton(
                getTranslation(AuthConstants.AUTH_RESET_PASSWORD_SUBMIT_KEY),
                e -> handleSubmit(password.getValue(), confirmPassword.getValue()));
        submit.setWidthFull();

        Button backToLogin = ButtonHelper.createTertiaryButton(
                getTranslation(AuthConstants.AUTH_RESET_PASSWORD_BACK_TO_LOGIN_KEY),
                e -> NavigationHelper.navigateToLogin());
        backToLogin.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation(AuthConstants.COMMON_BACK_TO_HOME_KEY), e -> NavigationHelper.navigateToHome());
        backToHome.setWidthFull();

        formFields.add(password, confirmPassword, submit, backToLogin, backToHome);

        formContainer.add(title, formFields);
        wrapper.add(formContainer);
        add(wrapper);
    }

    /**
     * Sets the route parameter for this view.
     * This method is called by the router when navigating to this view.
     * It extracts the reset token from the URL parameter and validates it.
     * If the token is invalid, the user is redirected to the login page.
     *
     * @param event the before event containing routing information
     * @param parameter the route parameter (reset token)
     */
    @Override
    public void setParameter(final BeforeEvent event, final String parameter) {
        this.token = parameter;

        // Validate token
        if (!passwordResetService.isTokenValid(token)) {
            // Throw exception for invalid token - will be caught by EntityNotFoundErrorHandler
            throw new EntityNotFoundException(
                    parameter,
                    RouteConstants.LOGIN_ROUTE,
                    getTranslation(AuthConstants.AUTH_RESET_PASSWORD_INVALID_TOKEN_KEY));
        }
    }

    private void handleSubmit(final String password, final String confirmPassword) {
        // Validate password not empty
        if (password == null || password.trim().isEmpty()) {
            NotificationHelper.showError(getTranslation(AuthConstants.AUTH_RESET_PASSWORD_PASSWORD_REQUIRED_KEY));
            return;
        }

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            NotificationHelper.showError(getTranslation(AuthConstants.AUTH_RESET_PASSWORD_PASSWORD_MISMATCH_KEY));
            return;
        }

        // Validate password policy using centralized validator
        if (PasswordValidator.isInvalid(password)) {
            NotificationHelper.showError(getTranslation(AuthConstants.AUTH_RESET_PASSWORD_PASSWORD_TOO_SHORT_KEY));
            return;
        }

        try {
            boolean success = passwordResetService.resetPassword(token, password);
            if (success) {
                NotificationHelper.showSuccess(getTranslation(AuthConstants.AUTH_RESET_PASSWORD_SUCCESS_KEY));
                NavigationHelper.navigateToLogin();
            } else {
                NotificationHelper.showError(getTranslation(AuthConstants.AUTH_RESET_PASSWORD_FAILED_KEY));
            }
        } catch (Exception ex) {
            NotificationHelper.showError(getTranslation(AuthConstants.AUTH_RESET_PASSWORD_ERROR_KEY));
        }
    }

    /**
     * Handles the before enter event for this view.
     * Redirects authenticated users to home page.
     *
     * @param event the before enter event containing navigation context
     */
    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        AuthRedirectHelper.redirectAuthenticatedToHome(event);
    }

    /**
     * Gets the page title for the reset password view.
     *
     * @return the localized reset password title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(AuthConstants.AUTH_RESET_PASSWORD_TITLE_KEY);
    }
}
