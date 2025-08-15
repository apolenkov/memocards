package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "login", layout = PublicLayout.class)
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver, HasDynamicTitle {

    private static final class LoginModel {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private final transient AuthFacade authFacade;

    public LoginView(AuthFacade authFacade) {
        this.authFacade = authFacade;

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField username = new TextField(getTranslation("auth.login.username"));
        username.setWidth("420px");
        username.setRequiredIndicatorVisible(true);

        PasswordField password = new PasswordField(getTranslation("auth.login.password"));
        password.setWidth("420px");
        password.setRequiredIndicatorVisible(true);

        Button submit = new Button(getTranslation("auth.login.submit"));
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.setWidth("420px");

        Button forgot = new Button(getTranslation("auth.login.forgotPassword"));
        forgot.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        forgot.addClickListener(e -> Notification.show(getTranslation("auth.login.forgotUnsupported")));

        Binder<LoginModel> binder = new Binder<>(LoginModel.class);
        LoginModel model = new LoginModel();
        binder.setBean(model);

        binder.forField(username)
                .asRequired(getTranslation("vaadin.validation.username.required"))
                .bind(LoginModel::getUsername, LoginModel::setUsername);
        binder.forField(password)
                .asRequired(getTranslation("vaadin.validation.password.required"))
                .bind(LoginModel::getPassword, LoginModel::setPassword);

        submit.addClickListener(e -> {
            if (binder.validate().isOk()) {
                try {
                    authFacade.authenticateAndPersist(model.getUsername(), model.getPassword());
                    getUI().ifPresent(ui -> ui.navigate(""));
                } catch (Exception ex) {
                    Notification.show(getTranslation("auth.login.errorMessage"));
                }
            }
        });

        wrapper.add(username, password, submit, forgot);
        add(wrapper);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            event.rerouteTo("");
            return;
        }
        boolean hasError =
                event.getLocation().getQueryParameters().getParameters().containsKey("error");
        if (hasError) Notification.show(getTranslation("auth.login.errorMessage"));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("auth.login");
    }
}
