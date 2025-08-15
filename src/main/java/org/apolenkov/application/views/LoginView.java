package org.apolenkov.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "login", layout = PublicLayout.class)
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver {

    private final LoginForm loginForm;

    public LoginView() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        loginForm = new LoginForm();
        loginForm.setAction("/login");
        loginForm.setForgotPasswordButtonVisible(false);

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle(getTranslation("auth.login.subtitle"));
        i18n.getForm().setUsername(getTranslation("auth.login.username"));
        i18n.getForm().setPassword(getTranslation("auth.login.password"));
        i18n.getForm().setSubmit(getTranslation("auth.login.submit"));
        i18n.getErrorMessage().setTitle(getTranslation("auth.login.errorTitle"));
        i18n.getErrorMessage().setMessage(getTranslation("auth.login.errorMessage"));
        loginForm.setI18n(i18n);

        wrapper.add(loginForm);
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
        if (hasError) {
            loginForm.setError(true);
        }
    }
}
