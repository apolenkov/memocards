package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.config.RouteConstants;
import org.apolenkov.application.service.AuthFacade;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;

/**
 * User registration view with comprehensive form validation, security measures, and automatic login.
 */
@Route(value = "register", layout = PublicLayout.class)
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements HasDynamicTitle {

    /**
     * Authentication facade for handling user registration and login operations.
     */
    private final transient AuthFacade authFacade;

    /**
     * Email validator instance for validating email field format.
     */
    private final EmailValidator emailValidator = new EmailValidator("invalid");

    /**
     * Text field for user's full name input.
     */
    private TextField name;

    /**
     * Email field for user's email address input with built-in validation.
     */
    private EmailField email;

    /**
     * Password field for user's password input with masking.
     */
    private PasswordField password;

    /**
     * Password confirmation field to ensure password accuracy.
     */
    private PasswordField confirm;

    /**
     * Creates a new RegisterView with authentication facade dependency.
     *
     * @param facade service for handling user registration and authentication
     */
    public RegisterView(final AuthFacade facade) {
        this.authFacade = facade;

        VerticalLayout wrapper = LayoutHelper.createCenteredVerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(Alignment.CENTER);
        wrapper.setJustifyContentMode(JustifyContentMode.CENTER);

        Div formContainer = new Div();
        formContainer.addClassName("register-form");
        formContainer.addClassName("auth-form");
        formContainer.addClassName("surface-panel");

        Div titleDiv = new Div();
        titleDiv.addClassName("register-form__title-container");

        Div title = new Div();
        title.setText(getTranslation("auth.register.title"));
        title.addClassName("register-form__title");
        titleDiv.add(title);

        FormLayout form = createForm();
        Button submit = createSubmitButton();
        Button backToHome = createBackToHomeButton();

        formContainer.add(titleDiv, form, submit, backToHome);
        wrapper.add(formContainer);
        add(wrapper);
    }

    /**
     * Creates and configures the registration form with all input fields.
     *
     * @return configured FormLayout with all registration input fields
     */
    private FormLayout createForm() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2));

        name = new TextField(getTranslation("auth.name"));
        name.setPlaceholder(getTranslation("auth.name.placeholder"));
        name.setWidthFull();
        name.setRequiredIndicatorVisible(true);

        email = new EmailField(getTranslation("auth.email"));
        email.setPlaceholder(getTranslation("auth.email.placeholder"));
        email.setWidthFull();
        email.setRequiredIndicatorVisible(true);

        password = new PasswordField(getTranslation("auth.password"));
        password.setPlaceholder(getTranslation("auth.password.placeholder"));
        password.setWidthFull();
        password.setRequiredIndicatorVisible(true);

        confirm = new PasswordField(getTranslation("auth.password.confirm"));
        confirm.setPlaceholder(getTranslation("auth.password.confirm.placeholder"));
        confirm.setWidthFull();
        confirm.setRequiredIndicatorVisible(true);

        form.add(name, email, password, confirm);
        return form;
    }

    /**
     * Creates the primary submit button for the registration form.
     * Configures a full-width primary button that triggers the registration
     * process when clicked. The button text is localized and styled using
     * the application's button helper utilities.
     *
     * @return configured submit Button with registration event handler
     */
    private Button createSubmitButton() {
        Button submit = ButtonHelper.createPrimaryButton(getTranslation("auth.register"), e -> handleRegistration());
        submit.setWidthFull();
        return submit;
    }

    /**
     * Creates the "Back to Home" navigation button.
     * Provides users with a way to return to the home page without
     * completing the registration. The button is styled as tertiary to
     * indicate it's a secondary action.
     *
     * @return configured tertiary Button that navigates to home page
     */
    private Button createBackToHomeButton() {
        Button backToHome =
                ButtonHelper.createTertiaryButton(getTranslation("common.backToHome"), e -> getUI().ifPresent(
                                ui -> ui.navigate(RouteConstants.HOME_ROUTE)));
        backToHome.setWidthFull();
        return backToHome;
    }

    /**
     * Handles the registration form submission process.
     * Orchestrates the complete registration workflow including form
     * validation, error handling, and successful registration processing.
     * Shows appropriate error messages if validation fails.
     */
    private void handleRegistration() {
        clearValidationErrors();

        if (!validateForm()) {
            NotificationHelper.showError(getTranslation("auth.validation.fixErrors"));
            return;
        }

        performRegistration();
    }

    /**
     * Clears all validation error states from form fields.
     * Resets the invalid state of all form fields to provide a clean
     * slate before performing new validation. This ensures that previous
     * validation errors don't persist inappropriately.
     */
    private void clearValidationErrors() {
        name.setInvalid(false);
        email.setInvalid(false);
        password.setInvalid(false);
        confirm.setInvalid(false);
    }

    /**
     * Validates all form fields and returns overall validation status.
     * Performs comprehensive validation of all registration form fields
     * including name, email, password, and password confirmation. Each
     * field is validated independently to show specific error messages.
     *
     * @return true if all form fields are valid, false otherwise
     */
    private boolean validateForm() {
        boolean nameValid = validateName();
        boolean emailValid = validateEmail();
        boolean passwordValid = validatePassword();
        boolean confirmValid = validatePasswordConfirmation();

        return nameValid && emailValid && passwordValid && confirmValid;
    }

    /**
     * Validates the name field according to business rules.
     * Ensures the name field is not empty and meets minimum length
     * requirements. Sets appropriate error messages and invalid state
     * if validation fails.
     *
     * @return true if name is valid, false otherwise
     */
    private boolean validateName() {
        String vName = name.getValue() == null ? "" : name.getValue().trim();

        if (vName.isEmpty()) {
            name.setErrorMessage(getTranslation("auth.validation.nameRequired"));
            name.setInvalid(true);
            return false;
        }

        if (vName.length() < 2) {
            name.setErrorMessage(getTranslation("auth.validation.nameMin2"));
            name.setInvalid(true);
            return false;
        }

        return true;
    }

    /**
     * Validates the email field for required value and format.
     * Checks that the email field is not empty and contains a valid
     * email format using the configured email validator. Sets appropriate
     * error messages for different validation failures.
     *
     * @return true if email is valid, false otherwise
     */
    private boolean validateEmail() {
        String vEmail = email.getValue() == null ? "" : email.getValue().trim();

        if (vEmail.isEmpty()) {
            email.setErrorMessage(getTranslation("auth.validation.emailRequired"));
            email.setInvalid(true);
            return false;
        }

        if (emailValidator.apply(vEmail, null).isError()) {
            email.setErrorMessage(getTranslation("auth.validation.invalidEmail"));
            email.setInvalid(true);
            return false;
        }

        return true;
    }

    /**
     * Validates the password field according to security policy.
     * Ensures the password meets the application's security requirements
     * including minimum length, character type requirements, etc. Uses the
     * {@link #isPasswordValid(String)} helper method for validation logic.
     *
     * @return true if password is valid, false otherwise
     */
    private boolean validatePassword() {
        String vPwd = password.getValue() == null ? "" : password.getValue();

        if (!isPasswordValid(vPwd)) {
            String pwdPolicy = getTranslation("auth.validation.passwordPolicy");
            password.setErrorMessage(pwdPolicy);
            password.setInvalid(true);
            return false;
        }

        return true;
    }

    /**
     * Checks if a password meets the application's security requirements.
     * Validates that the password has at least 8 characters, contains
     * both letters and digits. This method encapsulates the password
     * policy rules in one place for consistency.
     *
     * @param pwd the password string to validate
     * @return true if password meets all security requirements, false otherwise
     */
    private boolean isPasswordValid(final String pwd) {
        if (pwd.length() < 8) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : pwd.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }

            if (hasLetter && hasDigit) {
                break;
            }
        }

        return hasLetter && hasDigit;
    }

    /**
     * Validates that password confirmation matches the original password.
     * Ensures that the user has correctly re-entered their password
     * by comparing the password and confirmation fields. Sets an error
     * message on the confirmation field if they don't match.
     *
     * @return true if passwords match, false otherwise
     */
    private boolean validatePasswordConfirmation() {
        String vPwd = password.getValue() == null ? "" : password.getValue();
        String vConfirm = confirm.getValue() == null ? "" : confirm.getValue();

        if (!vPwd.equals(vConfirm)) {
            confirm.setErrorMessage(getTranslation("auth.validation.passwordsMismatch"));
            confirm.setInvalid(true);
            return false;
        }

        return true;
    }

    /**
     * Performs the actual user registration and automatic login.
     * Handles the complete registration workflow including user creation,
     * automatic authentication, and navigation to the appropriate page.
     * Shows success or error notifications based on the outcome and
     * navigates to either the decks page (success) or login page (failure).
     */
    private void performRegistration() {
        String vEmail = email.getValue().trim();
        String vPwd = password.getValue();

        try {
            authFacade.registerUser(vEmail, vPwd);
            authFacade.authenticateAndPersist(vEmail, vPwd);
            NotificationHelper.showSuccess(getTranslation("auth.register.successLogin"));
            getUI().ifPresent(ui -> ui.navigate(RouteConstants.DECKS_ROUTE));
        } catch (Exception ex) {
            NotificationHelper.showError(getTranslation("auth.register.autoLoginFailed"));
            getUI().ifPresent(ui -> ui.navigate(RouteConstants.LOGIN_ROUTE));
        }
    }

    /**
     * Returns the page title for the registration view.
     * Provides a localized page title that appears in the browser tab
     * and navigation history.
     *
     * @return the localized page title for the registration page
     */
    @Override
    public String getPageTitle() {
        return getTranslation("auth.register");
    }
}
