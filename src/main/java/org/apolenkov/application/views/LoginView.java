package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.service.AuthFacade;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.FormHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * User authentication view with secure login interface, form validation, and navigation options.
 */
@Route(value = "login", layout = PublicLayout.class)
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    /**
     * Internal data model for the login form.
     * 
     * Encapsulates the form data and provides getter/setter methods
     * for binding with Vaadin's Binder framework.
     */
    private static final class LoginModel {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Creates a new LoginView with authentication facade dependency.
     * 
     * Creates a complete login form with proper validation, styling,
     * and event handling. The form is centered and styled using Lumo
     * theme components for consistent appearance.
     *
     * @param authFacade service for handling user authentication
     */
    public LoginView(AuthFacade authFacade) {
        VerticalLayout wrapper = LayoutHelper.createCenteredVerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.CENTER);

        // Create a beautiful Lumo-styled form container
        Div formContainer = new Div();
        formContainer.addClassName("login-form");
        formContainer.addClassName("auth-form");
        formContainer.addClassName("surface-panel");

        // Create form title
        Div titleDiv = new Div();
        titleDiv.addClassName("login-form__title-container");

        Div title = new Div();
        title.setText(getTranslation("auth.login"));
        title.addClassName("login-form__title");
        titleDiv.add(title);

        // Create form fields container
        VerticalLayout formFields = new VerticalLayout();
        formFields.setSpacing(true);
        formFields.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

        // Create binder and model first
        Binder<LoginModel> binder = new Binder<>(LoginModel.class);
        LoginModel model = new LoginModel();
        binder.setBean(model);

        TextField email = FormHelper.createRequiredTextField(
                getTranslation("auth.email"), getTranslation("auth.email.placeholder"));
        email.setWidthFull();

        PasswordField password = new PasswordField(getTranslation("auth.login.password"));
        password.setPlaceholder(getTranslation("auth.password.placeholder"));
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);

        Button submit = ButtonHelper.createPrimaryButton(getTranslation("auth.login.submit"), e -> {
            if (binder.validate().isOk()) {
                try {
                    authFacade.authenticateAndPersist(model.getEmail(), model.getPassword());
                    getUI().ifPresent(ui -> ui.navigate(""));
                } catch (Exception ex) {
                    NotificationHelper.showError(getTranslation("auth.login.errorMessage"));
                }
            }
        });
        submit.setWidthFull();

        Button forgot =
                ButtonHelper.createTertiaryButton(getTranslation("auth.login.forgotPassword"), e -> getUI().ifPresent(
                                ui -> ui.navigate("forgot-password")));
        forgot.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> getUI().ifPresent(ui -> ui.navigate("")));
        backToHome.setWidthFull();

        // Bind fields to model
        binder.forField(email)
                .asRequired(getTranslation("vaadin.validation.email.required"))
                .bind(LoginModel::getEmail, LoginModel::setEmail);
        binder.forField(password)
                .asRequired(getTranslation("vaadin.validation.password.required"))
                .bind(LoginModel::getPassword, LoginModel::setPassword);

        formFields.add(email, password, submit, forgot, backToHome);

        formContainer.add(titleDiv, formFields);
        wrapper.add(formContainer);
        add(wrapper);
    }

    /**
     * Handles navigation events before the view is entered.
     * Performs pre-navigation checks including:
     * - Redirecting already authenticated users to home page
     * - Displaying error messages for failed login attempts
     * - Handling query parameter-based error states
     *
     * @param event the before enter event containing navigation context
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            event.rerouteTo("");
            return;
        }
        boolean hasError =
                event.getLocation().getQueryParameters().getParameters().containsKey("error");
        if (hasError) NotificationHelper.showError(getTranslation("auth.login.errorMessage"));
    }

    /**
     * Returns the page title for the login view.
     * Provides a localized page title that appears in the browser tab
     * and navigation history.
     *
     * @return the localized page title for the login page
     */
    @Override
    public String getPageTitle() {
        return getTranslation("auth.login");
    }
}
