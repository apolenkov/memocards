package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.service.AuthFacade;

@Route(value = "register", layout = PublicLayout.class)
@PageTitle("Register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final transient AuthFacade authFacade;
    private final Binder<RegisterForm> binder = new Binder<>(RegisterForm.class);

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

        // Binder validation
        binder.forField(name)
                .asRequired(getTranslation("auth.validation.allRequired"))
                .withValidator(v -> v != null && v.trim().length() >= 2, getTranslation("auth.validation.allRequired"))
                .bind(RegisterForm::getName, RegisterForm::setName);
        binder.forField(email)
                .asRequired(getTranslation("auth.validation.allRequired"))
                .withValidator(new EmailValidator(getTranslation("auth.validation.userExists")))
                .bind(RegisterForm::getEmail, RegisterForm::setEmail);
        binder.forField(password)
                .asRequired(getTranslation("auth.validation.allRequired"))
                .withValidator(v -> v != null && v.length() >= 4, getTranslation("auth.validation.allRequired"))
                .bind(RegisterForm::getPassword, RegisterForm::setPassword);
        binder.forField(confirm)
                .asRequired(getTranslation("auth.validation.allRequired"))
                .withValidator(v -> v != null && v.length() >= 4, getTranslation("auth.validation.allRequired"))
                .bind(RegisterForm::getConfirm, RegisterForm::setConfirm);

        Button submit = new Button(getTranslation("auth.register"));
        submit.addClickListener(e -> {
            RegisterForm bean = new RegisterForm();
            try {
                binder.writeBean(bean);
            } catch (ValidationException ex) {
                Notification.show(getTranslation("auth.validation.allRequired"));
                return;
            }

            if (!bean.getPassword().equals(bean.getConfirm())) {
                Notification.show(getTranslation("auth.validation.passwordsMismatch"));
                return;
            }
            if (authFacade.userExists(bean.getEmail())) {
                Notification.show(getTranslation("auth.validation.userExists"));
                return;
            }

            try {
                authFacade.registerUser(bean.getEmail(), bean.getPassword());
                authFacade.authenticateAndPersist(bean.getEmail(), bean.getPassword());
                Notification.show(getTranslation("auth.register.successLogin"));
                getUI().ifPresent(ui -> ui.navigate("home"));
            } catch (Exception ex) {
                Notification.show(getTranslation("auth.register.autoLoginFailed"));
                getUI().ifPresent(ui -> ui.navigate("login"));
            }
        });

        form.add(name, email, password, confirm);
        add(title, form, submit);
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
