package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("")
@PageTitle("Flashcards — Learn smarter")
@AnonymousAllowed
@CssImport(value = "./themes/flashcards/views/home-view.css", themeFor = "vaadin-vertical-layout")
public class LandingView extends VerticalLayout {

    public LandingView() {
        addClassName("landing-view");
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        H1 title = new H1("Flashcards");
        Paragraph subtitle = new Paragraph(
                "Practice languages with beautiful, simple flashcards. Track progress and improve every day.");

        Image hero = new Image(
                new StreamResource(
                        "icon.png", () -> getClass().getResourceAsStream("/META-INF/resources/icons/icon.png")),
                "Flashcards");
        hero.setMaxWidth("160px");

        Button login = new Button("Login");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        login.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));

        Button register = new Button("Register");
        register.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));

        HorizontalLayout actions = new HorizontalLayout(login, register);
        actions.setSpacing(true);

        add(title, subtitle, hero, actions);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
}
