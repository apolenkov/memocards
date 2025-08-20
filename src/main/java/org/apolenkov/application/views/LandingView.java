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
        heroIcon.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        Image hero = new Image(
                new StreamResource("pixel-icon.svg", () -> getClass()
                        .getResourceAsStream("/META-INF/resources/icons/pixel-icon.svg")),
                getTranslation("landing.heroAlt"));

        hero.getStyle().set("width", "120px");
        hero.getStyle().set("height", "120px");

        // Simple click handler - always go to login
        hero.addClickListener(e -> NavigationHelper.navigateTo("login"));

        heroIcon.add(hero);

        H1 title = TextHelper.createMainTitle(getTranslation("app.title"));
        title.getStyle().set("margin", "0");
        title.getStyle().set("color", "var(--lumo-primary-text-color)");

        Paragraph subtitle = new Paragraph(getTranslation("landing.subtitle"));
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        subtitle.getStyle().set("font-size", "var(--lumo-font-size-l)");
        subtitle.getStyle().set("margin-bottom", "var(--lumo-space-l)");
        subtitle.getStyle().set("text-align", "center");

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
        newsSection
                .getStyle()
                .set("width", "100%")
                .set("max-width", "800px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)");

        H3 newsTitle = TextHelper.createSectionTitle(getTranslation("landing.news"));
        newsTitle.getStyle().set("margin", "0");
        newsTitle.getStyle().set("color", "var(--lumo-primary-text-color)");
        newsSection.add(newsTitle);

        Div newsList = new Div();
        newsList.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)");

        for (var item : newsService.getAllNews()) {
            Div card = new Div();
            card.getStyle()
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("border", "1px solid var(--lumo-contrast-10pct)")
                    .set("border-radius", "var(--lumo-border-radius-l)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("display", "flex")
                    .set("flex-direction", "column")
                    .set("gap", "var(--lumo-space-s)");

            H3 cardTitle = new H3(item.getTitle());
            cardTitle.getStyle().set("margin", "0");
            cardTitle.getStyle().set("color", "var(--lumo-primary-text-color)");
            card.add(cardTitle);

            Paragraph cardContent = new Paragraph(item.getContent());
            cardContent.getStyle().set("color", "var(--lumo-secondary-text-color)");
            card.add(cardContent);

            // Optional astronaut accent element
            Div astronaut = new Div();
            astronaut
                    .getStyle()
                    .set("width", "36px")
                    .set("height", "36px")
                    .set("background-image", "url('themes/flashcards/assets/modern-icons.svg')")
                    .set("background-size", "contain")
                    .set("background-repeat", "no-repeat")
                    .set("opacity", "0.9");
            // Place accent to the right side of the card header
            astronaut.getStyle().set("align-self", "flex-end");
            card.add(astronaut);

            newsList.add(card);
        }

        // Hero section container
        Div heroSection = new Div();
        heroSection
                .getStyle()
                .set("width", "100%")
                .set("max-width", "800px")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-m)");

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
