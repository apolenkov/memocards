package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
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
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.apolenkov.application.views.utils.TextHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "", layout = PublicLayout.class)
@AnonymousAllowed
public class LandingView extends VerticalLayout implements HasDynamicTitle {

    public LandingView(NewsService newsService) {
        setSpacing(true);
        setPadding(true);
        setWidthFull();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Div heroIcon = new Div();
        heroIcon.addClassName("landing-hero__icon");

        Image hero = new Image(
                new StreamResource("pixel-icon.svg", () -> getClass()
                        .getResourceAsStream("/META-INF/resources/icons/pixel-icon.svg")),
                getTranslation("landing.heroAlt"));

        hero.addClassName("landing-hero__image");

        // Smart click handler - go to decks if authenticated, login if not
        hero.addClickListener(e -> {
            if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
                // User is authenticated, go to decks
                NavigationHelper.navigateTo("decks");
            } else {
                // User is not authenticated, go to login
                NavigationHelper.navigateTo("login");
            }
        });

        heroIcon.add(hero);

        H1 title = TextHelper.createMainTitle(getTranslation("app.title"));
        title.addClassName("landing-hero__title");

        Paragraph subtitle = new Paragraph(getTranslation("landing.subtitle"));
        subtitle.addClassName("landing-hero__subtitle");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            Button login = ButtonHelper.createPrimaryButton(
                    getTranslation("auth.login"), e -> NavigationHelper.navigateTo("login"));

            Button register = ButtonHelper.createTertiaryButton(
                    getTranslation("auth.register"), e -> NavigationHelper.navigateTo("register"));

            actions.add(login, register);
        } else {
            boolean hasUser =
                    auth.getAuthorities().stream().anyMatch(a -> SecurityConstants.ROLE_USER.equals(a.getAuthority()));
            if (hasUser) {
                Button goToDecks = ButtonHelper.createPrimaryButton(
                        getTranslation("landing.goToDecks"), e -> NavigationHelper.navigateTo("decks"));
                actions.add(goToDecks);
            }
        }

        // News section wrapper
        Div newsSection = new Div();
        newsSection.addClassName("surface-panel");
        newsSection.addClassName("landing-news__section");

        H3 newsTitle = TextHelper.createSectionTitle(getTranslation("landing.news"));
        newsTitle.addClassName("landing-news__title");
        newsSection.add(newsTitle);

        Div newsList = new Div();
        newsList.addClassName("landing-news__list");

        for (var item : newsService.getAllNews()) {
            Div card = new Div();
            card.addClassName("surface-card");
            card.addClassName("landing-news__card");

            H3 cardTitle = new H3(item.getTitle());
            cardTitle.addClassName("landing-news__card-title");
            card.add(cardTitle);

            Paragraph cardContent = new Paragraph(item.getContent());
            cardContent.addClassName("landing-news__card-content");
            card.add(cardContent);

            // Optional astronaut accent element
            Div astronaut = new Div();
            astronaut.addClassName("landing-news__card-accent");
            card.add(astronaut);

            newsList.add(card);
        }

        // Hero section container
        Div heroSection = new Div();
        heroSection.addClassName("surface-panel");
        heroSection.addClassName("landing-hero__section");

        heroSection.add(heroIcon, title, subtitle, actions);

        newsSection.add(newsList);

        add(heroSection, newsSection);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("app.title");
    }
}
