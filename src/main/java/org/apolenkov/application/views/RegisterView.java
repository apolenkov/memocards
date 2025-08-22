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
import org.apolenkov.application.service.AuthFacade;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.LayoutHelper;
import org.apolenkov.application.views.utils.NotificationHelper;

/**
 * User registration view for the application.
 *
 * <p>This view provides a comprehensive registration interface for new users
 * to create accounts with proper validation and security measures. It includes
 * form validation for all required fields and automatic login after successful
 * registration.</p>
 *
 * <p>The registration form includes:</p>
 * <ul>
 *   <li>Name, email, and password input fields with validation</li>
 *   <li>Password confirmation field with matching validation</li>
 *   <li>Real-time form validation with user feedback</li>
 *   <li>Password strength requirements enforcement</li>
 *   <li>Automatic login after successful registration</li>
 * </ul>
 *
 * <p>The view enforces strong password policies and provides clear
 * error messages for validation failures.</p>
 */
@Route(value = "register", layout = PublicLayout.class)
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements HasDynamicTitle {

    @SuppressWarnings("unused")
    private final transient AuthFacade authFacade;

    private final EmailValidator emailValidator = new EmailValidator("invalid");

    /**
     * Constructs a new RegisterView with authentication facade dependency.
     *
     * <p>Creates a complete registration form with proper validation,
     * styling, and event handling. The form is centered and styled
     * using Lumo theme components for consistent appearance.</p>
     *
     * @param authFacade service for handling user registration and authentication
     */
    public RegisterView(AuthFacade authFacade) {
        this.authFacade = authFacade;

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

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("600px", 2));

        TextField name = new TextField(getTranslation("auth.name"));
        name.setPlaceholder(getTranslation("auth.name.placeholder"));
        name.setWidthFull();

        EmailField email = new EmailField(getTranslation("auth.email"));
        email.setPlaceholder(getTranslation("auth.email.placeholder"));
        email.setWidthFull();

        PasswordField password = new PasswordField(getTranslation("auth.password"));
        password.setPlaceholder(getTranslation("auth.password.placeholder"));
        password.setWidthFull();

        PasswordField confirm = new PasswordField(getTranslation("auth.password.confirm"));
        confirm.setPlaceholder(getTranslation("auth.password.confirm.placeholder"));
        confirm.setWidthFull();

        name.setRequiredIndicatorVisible(true);
        email.setRequiredIndicatorVisible(true);
        password.setRequiredIndicatorVisible(true);
        confirm.setRequiredIndicatorVisible(true);

        Button submit = ButtonHelper.createPrimaryButton(getTranslation("auth.register"), e -> {
            name.setInvalid(false);
            email.setInvalid(false);
            password.setInvalid(false);
            confirm.setInvalid(false);

            boolean ok = true;
            String pwdPolicy = getTranslation("auth.validation.passwordPolicy");

            String vName = name.getValue() == null ? "" : name.getValue().trim();
            if (vName.isEmpty()) {
                name.setErrorMessage(getTranslation("auth.validation.nameRequired"));
                name.setInvalid(true);
                ok = false;
            } else if (vName.length() < 2) {
                name.setErrorMessage(getTranslation("auth.validation.nameMin2"));
                name.setInvalid(true);
                ok = false;
            }

            String vEmail = email.getValue() == null ? "" : email.getValue().trim();
            if (vEmail.isEmpty()) {
                email.setErrorMessage(getTranslation("auth.validation.emailRequired"));
                email.setInvalid(true);
                ok = false;
            } else if (emailValidator.apply(vEmail, null).isError()) {
                email.setErrorMessage(getTranslation("auth.validation.invalidEmail"));
                email.setInvalid(true);
                ok = false;
            }

            String vPwd = password.getValue() == null ? "" : password.getValue();
            if (vPwd.length() < 8 || !vPwd.matches(".*[A-Za-z].*") || !vPwd.matches(".*\\d.*")) {
                password.setErrorMessage(pwdPolicy);
                password.setInvalid(true);
                ok = false;
            }

            String vConfirm = confirm.getValue() == null ? "" : confirm.getValue();
            if (!vPwd.equals(vConfirm)) {
                confirm.setErrorMessage(getTranslation("auth.validation.passwordsMismatch"));
                confirm.setInvalid(true);
                ok = false;
            }

            if (!ok) {
                NotificationHelper.showError(getTranslation("auth.validation.fixErrors"));
                return;
            }

            try {
                authFacade.registerUser(vEmail, vPwd);
                authFacade.authenticateAndPersist(vEmail, vPwd);
                NotificationHelper.showSuccess(getTranslation("auth.register.successLogin"));
                getUI().ifPresent(ui -> ui.navigate("decks"));
            } catch (Exception ex) {
                NotificationHelper.showError(getTranslation("auth.register.autoLoginFailed"));
                getUI().ifPresent(ui -> ui.navigate("login"));
            }
        });
        submit.setWidthFull();

        Button backToHome = ButtonHelper.createTertiaryButton(
                getTranslation("common.backToHome"), e -> getUI().ifPresent(ui -> ui.navigate("")));
        backToHome.setWidthFull();

        form.add(name, email, password, confirm);

        formContainer.add(titleDiv, form, submit, backToHome);

        wrapper.add(formContainer);

        add(wrapper);
    }

    /**
     * Returns the page title for the registration view.
     *
     * <p>Provides a localized page title that appears in the browser tab
     * and navigation history.</p>
     *
     * @return the localized page title for the registration page
     */
    @Override
    public String getPageTitle() {
        return getTranslation("auth.register");
    }

    /**
     * Internal data model for the registration form.
     *
     * <p>Encapsulates the form data and provides getter/setter methods
     * for form handling and validation.</p>
     */
    public static class RegisterForm {
        private String name;
        private String email;
        private String password;
        private String confirm;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

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

        public String getConfirm() {
            return confirm;
        }

        public void setConfirm(String confirm) {
            this.confirm = confirm;
        }
    }
}
