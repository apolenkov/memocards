package org.apolenkov.application.views;

import static org.apolenkov.application.config.RouteConstants.LOGIN_ROUTE;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
import org.apolenkov.application.config.RouteConstants;
import org.apolenkov.application.service.PasswordResetService;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
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
     * @param passwordResetService service for handling password reset operations
     */
    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;

        VerticalLayout wrapper = LayoutHelper.createCenteredVerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER);

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
        formFields.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

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
                getTranslation("auth.resetPassword.backToLogin"),
                e -> getUI().ifPresent(ui -> ui.navigate(LOGIN_ROUTE)));
        backToLogin.setWidthFull();

        Button backToHome =
                ButtonHelper.createTertiaryButton(getTranslation("common.backToHome"), e -> getUI().ifPresent(
                                ui -> ui.navigate(RouteConstants.HOME_ROUTE)));
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

    @Override
    public void setParameter(BeforeEvent event, String parameter) {
        this.token = parameter;

        // Validate token
        if (!passwordResetService.isTokenValid(token)) {
            NotificationHelper.showError(getTranslation("auth.resetPassword.invalidToken"));
            getUI().ifPresent(ui -> ui.navigate(LOGIN_ROUTE));
        }
    }

    private void handleSubmit(String password, String confirmPassword) {
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
                getUI().ifPresent(ui -> ui.navigate(LOGIN_ROUTE));
            } else {
                NotificationHelper.showError(getTranslation("auth.resetPassword.failed"));
            }
        } catch (Exception ex) {
            NotificationHelper.showError(getTranslation("auth.resetPassword.error"));
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            event.rerouteTo("");
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

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
