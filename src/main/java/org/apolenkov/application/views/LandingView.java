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

/**
 * Landing page view for the application.
 *
 * <p>This view serves as the main entry point for users, providing an attractive
 * introduction to the application with hero section, news updates, and appropriate
 * navigation options based on authentication status.</p>
 *
 * <p>The landing page features:</p>
 * <ul>
 *   <li>Hero section with application logo, title, and subtitle</li>
 *   <li>Dynamic action buttons based on user authentication state</li>
 *   <li>News section displaying recent application updates</li>
 *   <li>Responsive design with proper spacing and styling</li>
 * </ul>
 *
 * <p>The view automatically adapts its content based on whether the user is
 * authenticated, showing appropriate navigation options and greetings.</p>
 */
@Route(value = "", layout = PublicLayout.class)
@AnonymousAllowed
public class LandingView extends VerticalLayout implements HasDynamicTitle {

    /**
     * Creates a new LandingView with news service dependency.
     *
     * <p>Creates the complete landing page layout including hero section,
     * news updates, and dynamic action buttons. The layout automatically
     * adjusts based on the user's authentication status.</p>
     *
     * @param newsService service for retrieving and displaying news content
     */
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

        hero.addClickListener(e -> {
            if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
                NavigationHelper.navigateTo("decks");
            } else {
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

            Div astronaut = new Div();
            astronaut.addClassName("landing-news__card-accent");
            card.add(astronaut);

            newsList.add(card);
        }

        Div heroSection = new Div();
        heroSection.addClassName("surface-panel");
        heroSection.addClassName("landing-hero__section");

        heroSection.add(heroIcon, title, subtitle, actions);

        newsSection.add(newsList);

        add(heroSection, newsSection);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    /**
     * Returns the page title for the landing view.
     *
     * <p>Provides a localized page title that appears in the browser tab
     * and navigation history.</p>
     *
     * @return the localized page title
     */
    @Override
    public String getPageTitle() {
        return getTranslation("app.title");
    }
}
