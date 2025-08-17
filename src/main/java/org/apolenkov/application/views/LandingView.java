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
@CssImport(value = "./themes/flashcards/views/landing-view.css", themeFor = "vaadin-vertical-layout")
@CssImport(value = "./themes/flashcards/views/landing-view-extra.css", themeFor = "vaadin-vertical-layout")
public class LandingView extends VerticalLayout implements HasDynamicTitle {

    private final NewsService newsService;

    public LandingView(NewsService newsService) {
        this.newsService = newsService;
        addClassName("landing-view");
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Div heroIcon = new Div();
        heroIcon.addClassName("landing-view__hero-icon");

        Image hero = new Image(
                new StreamResource("pixel-icon.svg", () -> getClass()
                        .getResourceAsStream("/META-INF/resources/icons/pixel-icon.svg")),
                getTranslation("landing.heroAlt"));
        hero.addClassName("landing-view__hero");

        // Add click handler for hero element - redirect to decks if user is authenticated with USER role
        hero.addClickListener(e -> {
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            if (currentAuth != null && !(currentAuth instanceof AnonymousAuthenticationToken)) {
                boolean hasUserRole = currentAuth.getAuthorities().stream()
                        .anyMatch(a -> SecurityConstants.ROLE_USER.equals(a.getAuthority()));
                if (hasUserRole) {
                    getUI().ifPresent(ui -> ui.navigate("decks"));
                }
            } else {
                // If anonymous or not authenticated, redirect to login
                getUI().ifPresent(ui -> ui.navigate("login"));
            }
        });

        heroIcon.add(hero);

        H1 title = new H1(getTranslation("app.title"));
        title.addClassName("landing-view__title");

        Paragraph subtitle = new Paragraph(getTranslation("landing.subtitle"));
        subtitle.addClassName("landing-view__subtitle");

        // Decorative astronaut element (no JS, animated via CSS)
        Div astronaut = new Div();
        astronaut.addClassName("landing-view__astronaut");
        heroIcon.add(astronaut);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.addClassName("landing-view__actions");

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            Button login = new Button(getTranslation("auth.login"));
            login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            login.addClassName("landing-view__login-btn");
            login.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));

            Button register = new Button(getTranslation("auth.register"));
            register.addClassName("landing-view__register-btn");
            register.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));

            actions.add(login, register);
        } else {
            boolean hasUser =
                    auth.getAuthorities().stream().anyMatch(a -> SecurityConstants.ROLE_USER.equals(a.getAuthority()));
            if (hasUser) {
                Button goToDecks = new Button(
                        getTranslation("landing.goToDecks"), e -> getUI().ifPresent(ui -> ui.navigate("decks")));
                goToDecks.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                goToDecks.addClassName("landing-view__decks-btn");
                actions.add(goToDecks);
            }
        }

        Div newsBlock = new Div();
        newsBlock.addClassName("landing-view__news-block");

        H3 newsTitle = new H3(getTranslation("landing.news"));
        newsTitle.addClassName("landing-view__news-title");
        newsBlock.add(newsTitle);

        for (var item : this.newsService.getAllNews()) {
            Div card = new Div();
            card.addClassName("landing-view__news-card");

            H3 cardTitle = new H3(item.getTitle());
            cardTitle.addClassName("landing-view__news-card-title");
            card.add(cardTitle);

            Paragraph cardContent = new Paragraph(item.getContent());
            cardContent.addClassName("landing-view__news-card-content");
            card.add(cardContent);

            newsBlock.add(card);
        }

        add(title, subtitle, heroIcon, actions, newsBlock);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("app.title");
    }
}
