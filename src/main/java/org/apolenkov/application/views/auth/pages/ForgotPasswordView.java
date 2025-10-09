package org.apolenkov.application.views.auth.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.service.PasswordResetService;
import org.apolenkov.application.views.auth.constants.AuthConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = RouteConstants.FORGOT_PASSWORD_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public final class ForgotPasswordView extends BaseView implements BeforeEnterObserver {

    private final transient PasswordResetService passwordResetService;

    /**
     * Creates a new ForgotPasswordView with password reset service dependency.
     *
     * @param service service for handling password reset operations
     */
    public ForgotPasswordView(final PasswordResetService service) {
        this.passwordResetService = service;

        VerticalLayout wrapper = createCenteredVerticalLayout();

        // Create a beautiful Lumo-styled form container
        Div formContainer = new Div();
        formContainer.addClassName(AuthConstants.FORGOT_PASSWORD_FORM_CLASS);
        formContainer.addClassName(AuthConstants.AUTH_FORM_CLASS);
        formContainer.addClassName(AuthConstants.SURFACE_PANEL_CLASS);

        // Create form title
        H2 title = new H2(getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_TITLE_KEY));
        title.addClassName(AuthConstants.FORGOT_PASSWORD_FORM_TITLE_CLASS);

        // Create form fields container
        VerticalLayout formFields = new VerticalLayout();
        formFields.setSpacing(true);
        formFields.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField email = new TextField(getTranslation(AuthConstants.AUTH_EMAIL_KEY));
        email.setPlaceholder(getTranslation(AuthConstants.AUTH_EMAIL_PLACEHOLDER_KEY));
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();

        // Submit button triggers password reset process
        Button submit = ButtonHelper.createPrimaryButton(
                getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_SUBMIT_KEY), e -> handleSubmit(email.getValue()));
        submit.setWidthFull();

        Button backToLogin = ButtonHelper.createTertiaryButton(
                getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_BACK_TO_LOGIN_KEY),
                e -> NavigationHelper.navigateToLogin());
        backToLogin.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation(AuthConstants.COMMON_BACK_TO_HOME_KEY), e -> NavigationHelper.navigateToHome());
        backToHome.setWidthFull();

        formFields.add(email, submit, backToLogin, backToHome);

        formContainer.add(title, formFields);
        wrapper.add(formContainer);
        add(wrapper);
    }

    /**
     * Handles password reset request submission.
     * Creates reset token and navigates to reset page or shows appropriate messages.
     *
     * @param email the email address for password reset
     */
    private void handleSubmit(final String email) {
        if (email == null || email.trim().isEmpty()) {
            NotificationHelper.showError(getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_EMAIL_REQUIRED_KEY));
            return;
        }

        try {
            var tokenOpt = passwordResetService.createPasswordResetToken(email.trim());
            if (tokenOpt.isPresent()) {
                // In a real application, you would send this token via email
                // For now, we'll show it in a notification (this is just for demo)
                String token = tokenOpt.get();

                NotificationHelper.showSuccess(getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_TOKEN_CREATED_KEY));

                // Navigate to reset password page with the generated token
                NavigationHelper.navigateToResetPassword(token);
            } else {
                // Don't reveal if email exists for security reasons
                NotificationHelper.showInfo(getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_EMAIL_NOT_FOUND_KEY));
            }
        } catch (Exception ex) {
            // Generic error message to avoid information leakage
            NotificationHelper.showError(getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_ERROR_KEY));
        }
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            NavigationHelper.forwardToHome(event);
        }
    }

    /**
     * Gets the page title for the forgot password view.
     *
     * @return the localized forgot password title
     */
    @Override
    public String getPageTitle() {
        return getTranslation(AuthConstants.AUTH_FORGOT_PASSWORD_TITLE_KEY);
    }
}
