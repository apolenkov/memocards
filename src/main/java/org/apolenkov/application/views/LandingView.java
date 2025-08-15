package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.config.SecurityConstants;
import org.apolenkov.application.service.NewsService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "", layout = PublicLayout.class)
@AnonymousAllowed
@CssImport(value = "./themes/flashcards/views/home-view.css", themeFor = "vaadin-vertical-layout")
public class LandingView extends VerticalLayout implements HasDynamicTitle {

    private final NewsService newsService;

    public LandingView(NewsService newsService) {
        this.newsService = newsService;
        addClassName("landing-view");
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        H1 title = new H1(getTranslation("app.title"));
        Paragraph subtitle = new Paragraph(getTranslation("landing.subtitle"));

        Image hero = new Image(
                new StreamResource(
                        "icon.png", () -> getClass().getResourceAsStream("/META-INF/resources/icons/icon.png")),
                getTranslation("landing.heroAlt"));
        hero.setMaxWidth("160px");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            Button login = new Button(getTranslation("auth.login"));
            login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            login.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));

            Button register = new Button(getTranslation("auth.register"));
            register.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));

            actions.add(login, register);
        } else {
            boolean hasUser =
                    auth.getAuthorities().stream().anyMatch(a -> SecurityConstants.ROLE_USER.equals(a.getAuthority()));
            if (hasUser) {
                Button goToDecks = new Button(
                        getTranslation("landing.goToDecks"), e -> getUI().ifPresent(ui -> ui.navigate("decks")));
                goToDecks.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                actions.add(goToDecks);
            }
        }

        Div newsBlock = new Div();
        newsBlock.getStyle().set("max-width", "720px");
        newsBlock.getStyle().set("margin-top", "var(--lumo-space-l)");
        newsBlock.add(new H3(getTranslation("landing.news")));
        for (var item : this.newsService.getAllNews()) {
            Div card = new Div();
            card.getStyle()
                    .set("border", "1px solid var(--lumo-contrast-20pct)")
                    .set("border-radius", "8px")
                    .set("padding", "var(--lumo-space-m)")
                    .set("margin-bottom", "var(--lumo-space-m)");
            card.add(new H3(item.getTitle()));
            card.add(new Paragraph(item.getContent()));
            newsBlock.add(card);
        }

        add(title, subtitle, hero, actions, newsBlock);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("app.title");
    }
}
