package org.apolenkov.application.views.auth.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
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
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.auth.constants.AuthConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
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

/**
 * User authentication view with secure login interface, form validation, and navigation options.
 */
@Route(value = RouteConstants.LOGIN_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public class LoginView extends BaseView implements BeforeEnterObserver {

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
    @SuppressWarnings("unused")
    private void init() {
        VerticalLayout wrapper = createCenteredVerticalLayout();

        // Create a beautiful Lumo-styled form container
        Div formContainer = new Div();
        formContainer.addClassName(AuthConstants.LOGIN_FORM_CLASS);
        formContainer.addClassName(AuthConstants.AUTH_FORM_CLASS);
        formContainer.addClassName(AuthConstants.SURFACE_PANEL_CLASS);

        // Create form title
        Div titleDiv = new Div();
        titleDiv.addClassName(AuthConstants.LOGIN_FORM_TITLE_CONTAINER_CLASS);

        Div title = new Div();
        title.setText(getTranslation(AuthConstants.AUTH_LOGIN_KEY));
        title.addClassName(AuthConstants.LOGIN_FORM_TITLE_CLASS);
        titleDiv.add(title);

        // Create form fields container
        VerticalLayout formFields = new VerticalLayout();
        formFields.setSpacing(true);
        formFields.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField email = new TextField(getTranslation(AuthConstants.AUTH_EMAIL_KEY));
        email.setPlaceholder(getTranslation(AuthConstants.AUTH_EMAIL_PLACEHOLDER_KEY));
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();

        PasswordField password = new PasswordField(getTranslation(AuthConstants.AUTH_LOGIN_PASSWORD_KEY));
        password.setPlaceholder(getTranslation(AuthConstants.AUTH_PASSWORD_PLACEHOLDER_KEY));
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);

        Button submit = ButtonHelper.createPrimaryButton(getTranslation(AuthConstants.AUTH_LOGIN_SUBMIT_KEY), e -> {
            String emailValue = email.getValue();
            String passwordValue = password.getValue();

            if (emailValue == null || emailValue.trim().isEmpty()) {
                email.setInvalid(true);
                email.setErrorMessage(getTranslation(AuthConstants.VAADIN_VALIDATION_EMAIL_REQUIRED_KEY));
                return;
            }
            if (passwordValue == null || passwordValue.isEmpty()) {
                password.setInvalid(true);
                password.setErrorMessage(getTranslation(AuthConstants.VAADIN_VALIDATION_PASSWORD_REQUIRED_KEY));
                return;
            }

            try {
                authenticateAndPersist(emailValue, passwordValue);
                NavigationHelper.navigateToHome();
            } catch (IllegalArgumentException ex) {
                NotificationHelper.showError(ex.getMessage());
            } catch (Exception ex) {
                NotificationHelper.showError(getTranslation(AuthConstants.AUTH_LOGIN_ERROR_MESSAGE_KEY));
            }
        });
        submit.setWidthFull();

        Button forgot = ButtonHelper.createTertiaryButton(
                getTranslation(AuthConstants.AUTH_LOGIN_FORGOT_PASSWORD_KEY),
                e -> NavigationHelper.navigateToForgotPassword());
        forgot.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation(AuthConstants.COMMON_BACK_TO_HOME_KEY), e -> NavigationHelper.navigateToHome());
        backToHome.setWidthFull();

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
            NotificationHelper.showError(getTranslation(AuthConstants.AUTH_LOGIN_ERROR_MESSAGE_KEY));
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
        return getTranslation(AuthConstants.AUTH_LOGIN_KEY);
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
