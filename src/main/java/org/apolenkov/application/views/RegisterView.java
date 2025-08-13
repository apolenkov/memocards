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
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Route(value = "register", layout = PublicLayout.class)
@PageTitle("Register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final transient UserDetailsService userDetailsService;
    private final transient PasswordEncoder passwordEncoder;
    private final transient AuthenticationManager authenticationManager;

    public RegisterView(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;

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
                // Auto-login the user and persist security context in HTTP session
                try {
                    UsernamePasswordAuthenticationToken authRequest =
                            new UsernamePasswordAuthenticationToken(email.getValue(), password.getValue());
                    Authentication auth = authenticationManager.authenticate(authRequest);

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

                    Notification.show("Welcome, you are now logged in");
                    getUI().ifPresent(ui -> ui.navigate("home"));
                } catch (Exception ex) {
                    Notification.show("Registered, but auto-login failed. Please sign in.");
                    getUI().ifPresent(ui -> ui.navigate("login"));
                }
            } else {
                Notification.show("Registration is not available");
            }
        });

        form.add(name, email, password, confirm);
        add(title, form, submit);
    }
}
