package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.service.AuthFacade;

@Route(value = "register", layout = PublicLayout.class)
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements HasDynamicTitle {

    @SuppressWarnings("unused")
    private final transient AuthFacade authFacade;

    private final EmailValidator emailValidator = new EmailValidator("invalid");

    public RegisterView(AuthFacade authFacade) {
        this.authFacade = authFacade;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2(getTranslation("auth.register.title"));
        FormLayout form = new FormLayout();
        TextField name = new TextField(getTranslation("auth.name"));
        EmailField email = new EmailField(getTranslation("auth.email"));
        PasswordField password = new PasswordField(getTranslation("auth.password"));
        PasswordField confirm = new PasswordField(getTranslation("auth.password.confirm"));

        name.setRequiredIndicatorVisible(true);
        email.setRequiredIndicatorVisible(true);
        password.setRequiredIndicatorVisible(true);
        confirm.setRequiredIndicatorVisible(true);

        Button submit = new Button(getTranslation("auth.register"));
        submit.addClickListener(e -> {
            name.setInvalid(false);
            email.setInvalid(false);
            password.setInvalid(false);
            confirm.setInvalid(false);

            boolean ok = true;
            // keep allReq key for future form-level validation messages if needed
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
                Notification.show(getTranslation("auth.validation.fixErrors"));
                return;
            }

            try {
                authFacade.registerUser(vEmail, vPwd);
                authFacade.authenticateAndPersist(vEmail, vPwd);
                Notification.show(getTranslation("auth.register.successLogin"));
                getUI().ifPresent(ui -> ui.navigate(""));
            } catch (Exception ex) {
                Notification.show(getTranslation("auth.register.autoLoginFailed"));
                getUI().ifPresent(ui -> ui.navigate("login"));
            }
        });

        form.add(name, email, password, confirm);
        add(title, form, submit);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("auth.register");
    }

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
