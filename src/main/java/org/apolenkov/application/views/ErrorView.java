package org.apolenkov.application.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
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

        // Create a beautiful error container
        VerticalLayout errorContainer = new VerticalLayout();
        errorContainer.getStyle().set("background", "var(--lumo-contrast-5pct)");
        errorContainer.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        errorContainer.getStyle().set("padding", "var(--lumo-space-xl)");
        errorContainer.getStyle().set("max-width", "500px");
        errorContainer.getStyle().set("width", "100%");
        errorContainer.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        errorContainer.getStyle().set("text-align", "center");
        errorContainer.setSpacing(true);
        errorContainer.setAlignItems(Alignment.CENTER);

        H2 title = new H2(getTranslation("error.500"));
        title.getStyle().set("color", "var(--lumo-error-color)");
        title.getStyle().set("margin", "0 0 var(--lumo-space-m) 0");

        Span description = new Span(getTranslation("error.500.description"));
        description.getStyle().set("text-align", "center");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");
        description.getStyle().set("margin-bottom", "var(--lumo-space-l)");

        // Action buttons
        Button goHome = new Button(getTranslation("main.gohome"));
        goHome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goHome.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate("decks")));

        Button reload = new Button(getTranslation("error.reload"));
        reload.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        reload.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().reload()));

        HorizontalLayout buttons = new HorizontalLayout(goHome, reload);
        buttons.setSpacing(true);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        errorContainer.add(title, description, buttons);
        add(errorContainer);

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
        // Don't go back to main page to avoid infinite loops
        if (fromRoute.isEmpty() || fromRoute.equals("error") || fromRoute.isEmpty()) {
            return;
        }

        Button goBack = new Button(getTranslation("error.back"));
        goBack.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(fromRoute)));
        goBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        // Insert after title and description
        if (getComponentCount() > 2) {
            addComponentAtIndex(2, goBack);
        }
    }

    private void addDevInfo() {
        H3 devTitle = new H3(getTranslation("error.dev.title"));
        devTitle.getStyle().set("color", "var(--lumo-error-color)");
        devTitle.getStyle().set("font-size", "var(--lumo-font-size-s)");
        devTitle.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        // Create expandable dev info container
        VerticalLayout devContainer = new VerticalLayout();
        devContainer.getStyle().set("background", "var(--lumo-contrast-10pct)");
        devContainer.getStyle().set("border-radius", "var(--lumo-border-radius)");
        devContainer.getStyle().set("padding", "var(--lumo-space-m)");
        devContainer.getStyle().set("margin", "var(--lumo-space-m) 0");
        devContainer.getStyle().set("width", "100%");
        devContainer.getStyle().set("max-width", "800px");
        devContainer.setSpacing(true);

        // Error details section
        Div errorDetails = new Div();
        errorDetails.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        // Add error type and message
        Span errorType = new Span(getTranslation("error.type") + ": " + "General Error");
        errorType.getStyle().set("color", "var(--lumo-error-color)");
        errorType.getStyle().set("font-weight", "bold");
        errorType.getStyle().set("display", "block");
        errorType.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        Span errorMessage = new Span(getTranslation("error.message") + ": " + "An error occurred during navigation");
        errorMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
        errorMessage.getStyle().set("font-size", "var(--lumo-font-size-s)");
        errorMessage.getStyle().set("display", "block");
        errorMessage.getStyle().set("margin-bottom", "var(--lumo-space-s)");

        // Add current route info
        Span currentRoute = new Span(getTranslation("error.current.route") + ": " + "Error View");
        currentRoute.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        currentRoute.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        currentRoute.getStyle().set("display", "block");
        currentRoute.getStyle().set("margin-bottom", "var(--lumo-space-xs)");

        errorDetails.add(errorType, errorMessage, currentRoute);

        // Add timestamp
        Span timestamp = new Span(getTranslation("error.timestamp") + " "
                + java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timestamp.getStyle().set("font-size", "var(--lumo-font-size-xs)");
        timestamp.getStyle().set("color", "var(--lumo-tertiary-text-color)");
        timestamp.getStyle().set("display", "block");
        timestamp.getStyle().set("margin-top", "var(--lumo-space-s)");

        devContainer.add(devTitle, errorDetails, timestamp);
        add(devContainer);
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
