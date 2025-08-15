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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.apolenkov.application.domain.port.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Route(value = "", layout = PublicLayout.class)
@PageTitle("Flashcards — Learn smarter")
@AnonymousAllowed
@CssImport(value = "./themes/flashcards/views/home-view.css", themeFor = "vaadin-vertical-layout")
public class LandingView extends VerticalLayout {

    private final NewsRepository newsRepository;

    public LandingView(@Autowired(required = false) NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
        addClassName("landing-view");
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        // Получаем информацию об аутентификации
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        H1 title = new H1("Flashcards");
        Paragraph subtitle = new Paragraph(getTranslation("landing.subtitle"));

        Image hero = new Image(
                new StreamResource(
                        "icon.png", () -> getClass().getResourceAsStream("/META-INF/resources/icons/icon.png")),
                getTranslation("landing.heroAlt"));
        hero.setMaxWidth("160px");

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Показываем кнопки логина/регистрации только для неавторизованных пользователей
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            Button login = new Button(getTranslation("auth.login"));
            login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            login.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("login")));

            Button register = new Button(getTranslation("auth.register"));
            register.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("register")));

            actions.add(login, register);
        } else {
            // Если пользователь авторизован, показываем кнопку для перехода к колодам только для ROLE_USER
            boolean hasUser = auth.getAuthorities().stream().anyMatch(a -> "ROLE_USER".equals(a.getAuthority()));
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
        try {
            if (this.newsRepository != null) {
                for (var item : this.newsRepository.findAllOrderByCreatedDesc()) {
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
            }
        } catch (Exception ignored) {
        }

        add(title, subtitle, hero, actions, newsBlock);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
}
