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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.apolenkov.application.config.RouteConstants;
import org.springframework.core.env.Environment;

/**
 * Generic error page view with user-friendly error display.
 * Shows a formatted error message with navigation options.
 * In development profile, displays additional debugging information.
 */
@Route(value = "error", layout = PublicLayout.class)
@AnonymousAllowed
public final class ErrorView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    private final transient Environment environment;
    private String fromRoute;

    /**
     * Creates a new error view.
     *
     * @param env Spring environment for profile detection
     */
    public ErrorView(final Environment env) {
        this.environment = env;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        VerticalLayout errorContainer = new VerticalLayout();
        errorContainer.addClassName("error-container");
        errorContainer.addClassName("surface-panel");
        errorContainer.setSpacing(true);
        errorContainer.setAlignItems(Alignment.CENTER);

        H2 title = new H2(getTranslation("error.500"));
        title.addClassName("error-view__title");

        Span description = new Span(getTranslation("error.500.description"));
        description.addClassName("error-view__description");

        Button goHome = new Button(getTranslation("main.gohome"));
        goHome.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        goHome.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(RouteConstants.DECKS_ROUTE)));

        Button reload = new Button(getTranslation("error.reload"));
        reload.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        reload.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().reload()));

        HorizontalLayout buttons = new HorizontalLayout(goHome, reload);
        buttons.setSpacing(true);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        errorContainer.add(title, description, buttons);
        add(errorContainer);

        if (isDevProfile()) {
            addDevInfo();
        }
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParams = location.getQueryParameters();
        fromRoute =
                queryParams.getParameters().getOrDefault("from", List.of("")).getFirst();

        if (!fromRoute.isEmpty() && !fromRoute.equals("error")) {
            addGoBackButton();
        }
    }

    private void addGoBackButton() {
        if (fromRoute.isEmpty() || fromRoute.equals("error")) {
            return;
        }

        Button goBack = new Button(getTranslation("error.back"));
        goBack.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(fromRoute)));
        goBack.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        if (getComponentCount() > 2) {
            addComponentAtIndex(2, goBack);
        }
    }

    private void addDevInfo() {
        H3 devTitle = new H3(getTranslation("error.dev.title"));
        devTitle.addClassName("error-dev__title");

        VerticalLayout devContainer = new VerticalLayout();
        devContainer.addClassName("error-dev__container");
        devContainer.addClassName("surface-panel");
        devContainer.setSpacing(true);

        Div errorDetails = new Div();
        errorDetails.addClassName("error-dev__details");

        Span errorType = new Span(getTranslation("error.type") + ": " + "General Error");
        errorType.addClassName("error-dev__type");

        Span errorMessage = new Span(getTranslation("error.message") + ": " + "An error occurred during navigation");
        errorMessage.addClassName("error-dev__message");

        Span currentRoute = new Span(getTranslation("error.current.route") + ": " + "Error View");
        currentRoute.addClassName("error-dev__route");

        errorDetails.add(errorType, errorMessage, currentRoute);

        Span timestamp = new Span(getTranslation("error.timestamp") + " "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timestamp.addClassName("error-dev__timestamp");

        devContainer.add(devTitle, errorDetails, timestamp);
        add(devContainer);
    }

    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("dev");
    }

    @Override
    public String getPageTitle() {
        return getTranslation("error.500");
    }
}
