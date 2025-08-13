package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route(value = "register", layout = PublicLayout.class)
@PageTitle("Register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final transient UserDetailsService userDetailsService;
    private final transient PasswordEncoder passwordEncoder;

    public RegisterView(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Create your account");
        FormLayout form = new FormLayout();
        TextField name = new TextField("Name");
        EmailField email = new EmailField("Email");
        PasswordField password = new PasswordField("Password");
        PasswordField confirm = new PasswordField("Confirm password");

        Button submit = new Button("Register");
        submit.addClickListener(e -> {
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Notification.show("All fields are required");
                return;
            }
            if (!password.getValue().equals(confirm.getValue())) {
                Notification.show("Passwords do not match");
                return;
            }
            try {
                UserDetails existing = userDetailsService.loadUserByUsername(email.getValue());
                if (existing != null) {
                    Notification.show("User already exists");
                    return;
                }
            } catch (Exception ignored) {
                // user not found, proceed
            }

            if (userDetailsService
                    instanceof org.springframework.security.provisioning.InMemoryUserDetailsManager manager) {
                manager.createUser(User.withUsername(email.getValue())
                        .password(passwordEncoder.encode(password.getValue()))
                        .roles("USER")
                        .build());
                Notification.show("Registered! You can login now");
                getUI().ifPresent(ui -> ui.navigate("login"));
            } else {
                Notification.show("Registration is not available");
            }
        });

        form.add(name, email, password, confirm);
        add(title, form, submit);
    }
}
