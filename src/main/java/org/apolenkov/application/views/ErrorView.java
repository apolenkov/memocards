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
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.apolenkov.application.config.constants.RouteConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Generic error page view with user-friendly error display.
 * Shows a formatted error message with navigation options.
 * In development profile, displays additional debugging information.
 */
@Route(value = RouteConstants.ERROR_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public final class ErrorView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorView.class);
    private final transient Environment environment;

    private String fromRoute;
    private String errorType;
    private String errorMessage;

    /**
     * Creates a new error view.
     *
     * @param env Spring environment for profile detection
     */
    public ErrorView(final Environment env) {
        this.environment = env;
    }

    @PostConstruct
    private void initializeUI() {
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
        goHome.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(RouteConstants.HOME_ROUTE)));

        Button tryAgain = new Button(getTranslation("error.tryAgain"));
        tryAgain.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        tryAgain.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(fromRoute)));

        HorizontalLayout buttons = new HorizontalLayout(goHome, tryAgain);
        buttons.setSpacing(true);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        errorContainer.add(title, description, buttons);
        add(errorContainer);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParams = location.getQueryParameters();
        fromRoute =
                queryParams.getParameters().getOrDefault("from", List.of("")).getFirst();
        errorType =
                queryParams.getParameters().getOrDefault("error", List.of("")).getFirst();
        errorMessage =
                queryParams.getParameters().getOrDefault("message", List.of("")).getFirst();

        // If no fromRoute parameter and no error parameters, redirect to home page (user accessed error page directly)
        if (fromRoute.isEmpty()
                || (errorType == null || errorType.isEmpty())
                || (errorMessage == null || errorMessage.isEmpty())) {
            LOGGER.info("No fromRoute parameter and no error parameters, redirecting to home page");
            event.rerouteTo(RouteConstants.DECKS_ROUTE);
            return;
        }

        LOGGER.info("fromRoute = {}, errorType = {}, errorMessage = {}", fromRoute, errorType, errorMessage);

        // Add dev info after reading parameters
        if (isDevProfile()) {
            addDevInfo();
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

        Span errorTypeSpan = new Span(getTranslation("error.type") + ": "
                + (errorType != null && !errorType.isEmpty() ? errorType : getTranslation("error.unknown")));
        errorTypeSpan.addClassName("error-dev__type");

        Span errorMessageSpan = new Span(getTranslation("error.message") + ": "
                + (errorMessage != null && !errorMessage.isEmpty()
                        ? errorMessage
                        : getTranslation("error.no.message")));
        errorMessageSpan.addClassName("error-dev__message");

        Span currentRoute = new Span(getTranslation("error.current.route") + ": " + RouteConstants.ERROR_ROUTE);
        currentRoute.addClassName("error-dev__route");

        errorDetails.add(errorTypeSpan, errorMessageSpan, currentRoute);

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
