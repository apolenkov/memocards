package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.core.env.Environment;

@Route(value = "error", layout = PublicLayout.class)
@AnonymousAllowed
public class ErrorView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    private final transient Environment environment;
    private String fromRoute;

    public ErrorView(Environment environment) {
        this.environment = environment;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        H2 title = new H2(getTranslation("error.500"));
        Span description = new Span(getTranslation("error.500.description"));
        description.getStyle().set("text-align", "center");

        // Action buttons
        Button goHome = new Button(getTranslation("main.gohome"));
        goHome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goHome.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("")));

        Button reload = new Button(getTranslation("error.reload"));
        reload.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().reload()));

        HorizontalLayout buttons = new HorizontalLayout(goHome, reload);
        buttons.setSpacing(true);

        add(title, description, buttons);

        // Dev info (only in dev profile)
        if (isDevProfile()) {
            addDevInfo();
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParams = location.getQueryParameters();
        fromRoute = queryParams
                .getParameters()
                .getOrDefault("from", java.util.List.of(""))
                .getFirst();

        // Add "Go Back" button if we have a from route
        if (!fromRoute.isEmpty() && !fromRoute.equals("error")) {
            addGoBackButton();
        }
    }

    private void addGoBackButton() {
        Button goBack = new Button(getTranslation("error.back"));
        goBack.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(fromRoute)));
        goBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Insert after title and description
        if (getComponentCount() > 2) {
            addComponentAtIndex(2, goBack);
        }
    }

    private void addDevInfo() {
        H3 devTitle = new H3(getTranslation("error.dev.info"));
        devTitle.getStyle().set("color", "var(--lumo-error-color)");
        devTitle.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Span devText = new Span("This is a development environment. Check server logs for detailed error information.");
        devText.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        devText.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(devTitle, devText);
    }

    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return java.util.Arrays.asList(activeProfiles).contains("dev");
    }

    @Override
    public String getPageTitle() {
        return getTranslation("error.500");
    }
}
