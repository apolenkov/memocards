package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.exceptions.EntityNotFoundException;
import org.apolenkov.application.service.PasswordResetService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Password reset form view for completing password reset process.
 * Validates reset tokens and allows users to set new passwords.
 * Accessible only to anonymous users with valid reset tokens.
 */
@Route(value = "reset-password", layout = PublicLayout.class)
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout
        implements HasDynamicTitle, HasUrlParameter<String>, BeforeEnterObserver {

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
    private void init() {
        VerticalLayout wrapper = LayoutHelper.createCenteredVerticalLayout();

        // Create a beautiful Lumo-styled form container
        Div formContainer = new Div();
        formContainer.addClassName("reset-password-form");
        formContainer.addClassName("auth-form");
        formContainer.addClassName("surface-panel");

        // Create form title
        H2 title = new H2(getTranslation("auth.resetPassword.title"));
        title.addClassName("reset-password-form__title");

        // Create form fields container
        VerticalLayout formFields = new VerticalLayout();
        formFields.setSpacing(true);
        formFields.setAlignItems(FlexComponent.Alignment.CENTER);

        // Create binder and model
        Binder<ResetPasswordModel> binder = new Binder<>(ResetPasswordModel.class);
        ResetPasswordModel model = new ResetPasswordModel();
        binder.setBean(model);

        PasswordField password = new PasswordField(getTranslation("auth.password"));
        password.setPlaceholder(getTranslation("auth.password.placeholder"));
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);

        PasswordField confirmPassword = new PasswordField(getTranslation("auth.password.confirm"));
        confirmPassword.setPlaceholder(getTranslation("auth.password.confirm.placeholder"));
        confirmPassword.setWidthFull();
        confirmPassword.setRequiredIndicatorVisible(true);

        Button submit = ButtonHelper.createPrimaryButton(
                getTranslation("auth.resetPassword.submit"),
                e -> handleSubmit(model.getPassword(), model.getConfirmPassword()));
        submit.setWidthFull();

        Button backToLogin = ButtonHelper.createTertiaryButton(
                getTranslation("auth.resetPassword.backToLogin"), e -> NavigationHelper.navigateToLogin());
        backToLogin.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> NavigationHelper.navigateToHome());
        backToHome.setWidthFull();

        // Bind fields to model
        binder.forField(password)
                .asRequired(getTranslation("vaadin.validation.password.required"))
                .bind(ResetPasswordModel::getPassword, ResetPasswordModel::setPassword);

        binder.forField(confirmPassword)
                .asRequired(getTranslation("vaadin.validation.password.confirm.required"))
                .bind(ResetPasswordModel::getConfirmPassword, ResetPasswordModel::setConfirmPassword);

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
                    parameter, RouteConstants.LOGIN_ROUTE, getTranslation("auth.resetPassword.invalidToken"));
        }
    }

    private void handleSubmit(final String password, final String confirmPassword) {
        if (password == null || password.trim().isEmpty()) {
            NotificationHelper.showError(getTranslation("auth.resetPassword.passwordRequired"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            NotificationHelper.showError(getTranslation("auth.resetPassword.passwordMismatch"));
            return;
        }

        if (password.length() < 8) {
            NotificationHelper.showError(getTranslation("auth.resetPassword.passwordTooShort"));
            return;
        }

        try {
            boolean success = passwordResetService.resetPassword(token, password);
            if (success) {
                NotificationHelper.showSuccess(getTranslation("auth.resetPassword.success"));
                NavigationHelper.navigateToLogin();
            } else {
                NotificationHelper.showError(getTranslation("auth.resetPassword.failed"));
            }
        } catch (Exception ex) {
            NotificationHelper.showError(getTranslation("auth.resetPassword.error"));
        }
    }

    /**
     * Handles the before enter event for this view.
     * This method is called before the view is entered and can be used to perform
     * pre-navigation checks or redirects. In this case, it checks if the user
     * is already authenticated and redirects them to the home page if so.
     *
     * @param event the before enter event containing navigation context
     */
    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            NavigationHelper.forwardToHome(event);
        }
    }

    /**
     * Gets the page title for the reset password view.
     *
     * @return the localized reset password title
     */
    @Override
    public String getPageTitle() {
        return getTranslation("auth.resetPassword.title");
    }

    private static final class ResetPasswordModel {
        private String password;
        private String confirmPassword;

        public String getPassword() {
            return password;
        }

        public void setPassword(final String passwordValue) {
            this.password = passwordValue;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(final String confirmPasswordValue) {
            this.confirmPassword = confirmPasswordValue;
        }
    }
}
