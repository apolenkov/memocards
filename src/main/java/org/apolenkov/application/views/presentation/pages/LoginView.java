package org.apolenkov.application.views.presentation.pages;

import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.presentation.layouts.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * User authentication view with secure login interface, form validation, and navigation options.
 */
@Route(value = RouteConstants.LOGIN_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public class LoginView extends BaseView implements BeforeEnterObserver {

    /**
     * Internal data model for the login form.
     */
    private static final class LoginModel {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(final String emailValue) {
            this.email = emailValue;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(final String passwordValue) {
            this.password = passwordValue;
        }
    }

    private final transient AuthenticationConfiguration authenticationConfiguration;

    /**
     * Creates a new LoginView with authentication configuration dependency.
     *
     * @param authenticationConfigurationParam Spring Security authentication configuration
     */
    public LoginView(final AuthenticationConfiguration authenticationConfigurationParam) {
        this.authenticationConfiguration = authenticationConfigurationParam;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void init() {
        VerticalLayout wrapper = createCenteredVerticalLayout();

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
        formFields.setAlignItems(FlexComponent.Alignment.CENTER);

        // Create binder and model first
        Binder<LoginModel> binder = new Binder<>(LoginModel.class);
        LoginModel model = new LoginModel();
        binder.setBean(model);

        TextField email = new TextField(getTranslation("auth.email"));
        email.setPlaceholder(getTranslation("auth.email.placeholder"));
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();

        PasswordField password = new PasswordField(getTranslation("auth.login.password"));
        password.setPlaceholder(getTranslation("auth.password.placeholder"));
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);

        Button submit = ButtonHelper.createPrimaryButton(getTranslation("auth.login.submit"), e -> {
            if (binder.validate().isOk()) {
                try {
                    authenticateAndPersist(model.getEmail(), model.getPassword());
                    NavigationHelper.navigateToHome();
                } catch (IllegalArgumentException ex) {
                    // Validation error - show specific message
                    NotificationHelper.showError(ex.getMessage());
                } catch (Exception ex) {
                    // Generic error - show generic message
                    NotificationHelper.showError(getTranslation("auth.login.errorMessage"));
                }
            }
        });
        submit.setWidthFull();

        Button forgot = ButtonHelper.createTertiaryButton(
                getTranslation("auth.login.forgotPassword"), e -> NavigationHelper.navigateToForgotPassword());
        forgot.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> NavigationHelper.navigateToHome());
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
    public void beforeEnter(final BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            NavigationHelper.forwardToHome(event);
            return;
        }
        boolean hasError =
                event.getLocation().getQueryParameters().getParameters().containsKey("error");
        if (hasError) {
            NotificationHelper.showError(getTranslation("auth.login.errorMessage"));
        }
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

    /**
     * Authenticates user and persists authentication session.
     * Performs user authentication using Spring Security's authentication manager
     * and persists authentication context to HTTP session for subsequent requests.
     *
     * @param username email address of user to authenticate
     * @param rawPassword plain text password for authentication
     * @throws IllegalArgumentException if authentication fails due to invalid credentials
     */
    private void authenticateAndPersist(final String username, final String rawPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }

        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, rawPassword);
        Authentication auth;
        try {
            auth = authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            throw new IllegalArgumentException("Authentication failed", e);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        VaadinServletRequest vsr = (VaadinServletRequest) VaadinService.getCurrentRequest();
        VaadinServletResponse vsp = (VaadinServletResponse) VaadinService.getCurrentResponse();
        if (vsr != null && vsp != null) {
            HttpServletRequest req = vsr.getHttpServletRequest();
            HttpServletResponse resp = vsp.getHttpServletResponse();
            new HttpSessionSecurityContextRepository().saveContext(context, req, resp);
        }
    }
}
