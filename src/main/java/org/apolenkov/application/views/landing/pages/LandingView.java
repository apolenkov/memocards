package org.apolenkov.application.views.landing.pages;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.service.NewsService;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.base.BaseView;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Landing page view with hero section, news updates, and dynamic navigation based on authentication status.
 */
@Route(value = RouteConstants.HOME_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public class LandingView extends BaseView {

    private final transient NewsService newsService;

    /**
     * Creates a new LandingView with news service dependency.
     *
     * @param service service for retrieving and displaying news content
     */
    public LandingView(final NewsService service) {
        this.newsService = service;
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void init() {
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

        hero.addClickListener(e -> {
            if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
                NavigationHelper.navigateToDecks();
            } else {
                NavigationHelper.navigateToLogin();
            }
        });

        heroIcon.add(hero);

        H1 title = new H1(getTranslation("app.title"));
        title.addClassName("landing-hero__title");

        Paragraph subtitle = new Paragraph(getTranslation("landing.subtitle"));
        subtitle.addClassName("landing-hero__subtitle");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            Button login = ButtonHelper.createPrimaryButton(
                    getTranslation("auth.login"), e -> NavigationHelper.navigateToLogin());

            Button register = ButtonHelper.createTertiaryButton(
                    getTranslation("auth.register"), e -> NavigationHelper.navigateToRegister());

            actions.add(login, register);
        } else {
            boolean hasUser =
                    auth.getAuthorities().stream().anyMatch(a -> SecurityConstants.ROLE_USER.equals(a.getAuthority()));
            if (hasUser) {
                Button goToDecks = ButtonHelper.createPrimaryButton(
                        getTranslation("landing.goToDecks"), e -> NavigationHelper.navigateToDecks());
                actions.add(goToDecks);
            }
        }

        Div newsSection = new Div();
        newsSection.addClassName("surface-panel");
        newsSection.addClassName("landing-news__section");

        H3 newsTitle = new H3(getTranslation("landing.news"));
        newsTitle.addClassName("landing-news__title");
        newsSection.add(newsTitle);

        Div newsList = createNewsList();
        newsSection.add(newsList);

        Div heroSection = new Div();
        heroSection.addClassName("surface-panel");
        heroSection.addClassName("landing-hero__section");
        heroSection.add(heroIcon, title, subtitle, actions);

        add(heroSection, newsSection);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    /**
     * Creates a news list container with all news cards.
     *
     * @return a Div containing all news cards
     */
    private Div createNewsList() {
        Div newsList = new Div();
        newsList.addClassName("landing-news__list");

        for (var item : newsService.getAllNews()) {
            Div card = createNewsCard(item);
            newsList.add(card);
        }

        return newsList;
    }

    /**
     * Creates a single news card component.
     *
     * @param item the news item to display in the card
     * @return a Div representing the news card
     */
    private Div createNewsCard(final org.apolenkov.application.model.News item) {
        Div card = new Div();
        card.addClassName("surface-card");
        card.addClassName("landing-news__card");

        H3 cardTitle = new H3(item.getTitle());
        cardTitle.addClassName("landing-news__card-title");
        card.add(cardTitle);

        Paragraph cardContent = new Paragraph(item.getContent());
        cardContent.addClassName("landing-news__card-content");
        card.add(cardContent);

        Div astronaut = new Div();
        astronaut.addClassName("landing-news__card-accent");
        card.add(astronaut);

        return card;
    }

    /**
     * Returns the page title for the landing view.
     * Provides a localized page title that appears in the browser tab
     * and navigation history.
     *
     * @return the localized page title
     */
    @Override
    public String getPageTitle() {
        return getTranslation("app.title");
    }
}
