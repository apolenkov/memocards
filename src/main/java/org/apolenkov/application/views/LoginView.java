package org.apolenkov.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends Div {

    public LoginView() {
        LoginOverlay login = new LoginOverlay();
        login.setTitle("Flashcards");
        login.setDescription("Sign in to continue");
        login.setOpened(true);
        login.setForgotPasswordButtonVisible(false);
        login.setAction("login");
        login.addLoginListener(e -> login.close());
        add(login);
    }
}
