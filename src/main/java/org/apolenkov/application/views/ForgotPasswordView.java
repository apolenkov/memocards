package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.service.PasswordResetService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.FormHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "forgot-password", layout = PublicLayout.class)
@AnonymousAllowed
public class ForgotPasswordView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private static final String COMPONENT_WIDTH = "420px";

    private static final class ForgotPasswordModel {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    private final PasswordResetService passwordResetService;

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        VerticalLayout wrapper = LayoutHelper.createCenteredVerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER);

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
        formFields.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        // Create binder and model for form validation
        Binder<ForgotPasswordModel> binder = new Binder<>(ForgotPasswordModel.class);
        ForgotPasswordModel model = new ForgotPasswordModel();
        binder.setBean(model);

        // Create form fields with proper validation
        TextField email = FormHelper.createRequiredTextField(
                getTranslation("auth.email"), getTranslation("auth.email.placeholder"));
        email.setWidthFull();

        // Submit button triggers password reset process
        Button submit = ButtonHelper.createPrimaryButton(
                getTranslation("auth.forgotPassword.submit"), e -> handleSubmit(model.getEmail()));
        submit.setWidthFull();

        // Navigation buttons for better UX
        Button backToLogin = ButtonHelper.createTertiaryButton(
                getTranslation("auth.forgotPassword.backToLogin"), e -> getUI().ifPresent(ui -> ui.navigate("login")));
        backToLogin.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> getUI().ifPresent(ui -> ui.navigate("")));
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
     * Handles password reset request submission
     * Creates reset token and navigates to reset page or shows appropriate messages
     */
    private void handleSubmit(String email) {
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
                getUI().ifPresent(ui -> ui.navigate("reset-password/" + token));
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
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            event.rerouteTo("");
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("auth.forgotPassword.title");
    }
}
