package org.apolenkov.application.views.presentation.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.service.PasswordResetService;
import org.apolenkov.application.views.presentation.layouts.PublicLayout;
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

    private static final class ForgotPasswordModel {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(final String emailValue) {
            this.email = emailValue;
        }
    }

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
        formContainer.addClassName("forgot-password-form");
        formContainer.addClassName("auth-form");
        formContainer.addClassName("surface-panel");

        // Create form title
        H2 title = new H2(getTranslation("auth.forgotPassword.title"));
        title.addClassName("forgot-password-form__title");

        // Create form fields container with proper spacing and alignment
        VerticalLayout formFields = new VerticalLayout();
        formFields.setSpacing(true);
        formFields.setAlignItems(FlexComponent.Alignment.CENTER);

        // Create binder and model for form validation
        Binder<ForgotPasswordModel> binder = new Binder<>(ForgotPasswordModel.class);
        ForgotPasswordModel model = new ForgotPasswordModel();
        binder.setBean(model);

        // Create form fields with proper validation
        TextField email = new TextField(getTranslation("auth.email"));
        email.setPlaceholder(getTranslation("auth.email.placeholder"));
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();

        // Submit button triggers password reset process
        Button submit = ButtonHelper.createPrimaryButton(
                getTranslation("auth.forgotPassword.submit"), e -> handleSubmit(model.getEmail()));
        submit.setWidthFull();

        // Navigation buttons for better UX
        Button backToLogin = ButtonHelper.createTertiaryButton(
                getTranslation("auth.forgotPassword.backToLogin"), e -> NavigationHelper.navigateToLogin());
        backToLogin.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> NavigationHelper.navigateToHome());
        backToHome.setWidthFull();

        // Bind fields to model with validation messages
        binder.forField(email)
                .asRequired(getTranslation("vaadin.validation.email.required"))
                .bind(ForgotPasswordModel::getEmail, ForgotPasswordModel::setEmail);

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
            NotificationHelper.showError(getTranslation("auth.forgotPassword.emailRequired"));
            return;
        }

        try {
            var tokenOpt = passwordResetService.createPasswordResetToken(email.trim());
            if (tokenOpt.isPresent()) {
                // In a real application, you would send this token via email
                // For now, we'll show it in a notification (this is just for demo)
                String token = tokenOpt.get();

                NotificationHelper.showSuccess(getTranslation("auth.forgotPassword.tokenCreated"));

                // Navigate to reset password page with the generated token
                NavigationHelper.navigateToResetPassword(token);
            } else {
                // Don't reveal if email exists for security reasons
                NotificationHelper.showInfo(getTranslation("auth.forgotPassword.emailNotFound"));
            }
        } catch (Exception ex) {
            // Generic error message to avoid information leakage
            NotificationHelper.showError(getTranslation("auth.forgotPassword.error"));
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
        return getTranslation("auth.forgotPassword.title");
    }
}
