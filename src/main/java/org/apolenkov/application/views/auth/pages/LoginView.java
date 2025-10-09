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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.service.auth.AuthService;
import org.apolenkov.application.views.auth.constants.AuthConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.AuthRedirectHelper;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.apolenkov.application.views.shared.utils.NotificationHelper;

/**
 * User authentication view with secure login interface, form validation, and navigation options.
 */
@Route(value = RouteConstants.LOGIN_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public class LoginView extends BaseView implements BeforeEnterObserver {

    private final transient AuthService authService;

    /**
     * Creates a new LoginView with authentication service dependency.
     *
     * @param authServiceParam service for handling authentication operations
     */
    public LoginView(final AuthService authServiceParam) {
        this.authService = authServiceParam;
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
                authService.authenticateAndPersist(emailValue, passwordValue);
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
        if (AuthRedirectHelper.redirectAuthenticatedToHome(event)) {
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
}
