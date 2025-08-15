package org.apolenkov.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
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

    public LoginView() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        LoginForm form = new LoginForm();
        form.setAction("/login");

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle(getTranslation("auth.login"));
        i18n.getHeader().setDescription(getTranslation("auth.login.subtitle"));
        i18n.getForm().setTitle(getTranslation("auth.login"));
        i18n.getForm().setUsername(getTranslation("auth.login.username"));
        i18n.getForm().setPassword(getTranslation("auth.login.password"));
        i18n.getForm().setSubmit(getTranslation("auth.login.submit"));
        form.setI18n(i18n);

        form.addLoginListener(e -> {});
        form.addForgotPasswordListener(
                e -> Notification.show(getTranslation("auth.login.forgotUnsupported", "Not implemented")));

        form.setError(false);

        wrapper.add(form);
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
            Notification.show(getTranslation("auth.login.errorMessage"));
        }
    }
}
