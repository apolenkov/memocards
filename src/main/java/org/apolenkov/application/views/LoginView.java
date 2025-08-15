package org.apolenkov.application.views;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
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

    private final TextField username;
    private final PasswordField password;

    public LoginView() {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setSizeFull();
        wrapper.setAlignItems(FlexComponent.Alignment.CENTER);
        wrapper.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username = new TextField(getTranslation("auth.login.username"));
        username.setRequiredIndicatorVisible(true);
        username.setClearButtonVisible(true);
        username.setWidth("320px");
        username.setAutofocus(true);
        username.setErrorMessage(getTranslation("vaadin.validation.username.required"));

        password = new PasswordField(getTranslation("auth.login.password"));
        password.setRequiredIndicatorVisible(true);
        password.setWidth("320px");
        password.setErrorMessage(getTranslation("vaadin.validation.password.required"));

        Button submit = new Button(getTranslation("auth.login.submit"));
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickShortcut(Key.ENTER);
        submit.addClickListener(e -> doSubmit());

        wrapper.add(new Div(getTranslation("auth.login.subtitle")), username, password, submit);
        add(wrapper);
    }

    private void doSubmit() {
        boolean ok = true;
        username.setInvalid(false);
        password.setInvalid(false);
        String u = username.getValue() == null ? "" : username.getValue().trim();
        String p = password.getValue() == null ? "" : password.getValue();
        if (u.isEmpty()) {
            username.setInvalid(true);
            ok = false;
        }
        if (p.isEmpty()) {
            password.setInvalid(true);
            ok = false;
        }
        if (!ok) {
            Notification.show(getTranslation("auth.validation.fixErrors"));
            return;
        }
        getElement()
                .executeJs(
                        "var f=document.createElement('form');f.method='POST';f.action='/login';"
                                + "var u=document.createElement('input');u.type='hidden';u.name='username';u.value=$0;f.appendChild(u);"
                                + "var p=document.createElement('input');p.type='hidden';p.name='password';p.value=$1;f.appendChild(p);"
                                + "document.body.appendChild(f);f.submit();",
                        u,
                        p);
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
