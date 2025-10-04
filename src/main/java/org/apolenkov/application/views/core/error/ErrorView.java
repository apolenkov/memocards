package org.apolenkov.application.views.core.error;

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
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
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

    // Params routing
    private String fromRoute;
    private String errorType;
    private String errorMessage;
    private String errorId;

    // UI Components - created in @PostConstruct, updated in beforeEnter
    private VerticalLayout errorContainer;
    private VerticalLayout devContainer;
    private H2 title;
    private Span description;
    private Button goHome;
    private Button tryAgain;
    private H3 devTitle;
    private Span errorTypeSpan;
    private Span errorMessageSpan;
    private Span currentRoute;
    private Span timestamp;

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
        LOGGER.debug("Initializing ErrorView UI components");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        createErrorContainer();
        createNavigationButtons();
        createDevInfoContainer();

        LOGGER.debug("ErrorView UI initialization completed");
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        LOGGER.debug("Processing beforeEnter event for ErrorView");

        extractErrorParameters(event);
        LOGGER.info(
                "Extracted parameters: fromRoute={}, errorType={}, errorMessage={}, errorId={}",
                fromRoute,
                errorType,
                errorMessage,
                errorId);

        if (shouldRedirectToHome()) {
            LOGGER.info("No valid error parameters found, redirecting to home page");
            NavigationHelper.forwardToHome(event);
            return;
        }

        // Security: Only log error details, never expose in UI for production
        if (isDevProfile()) {
            LOGGER.info(
                    "Processing error in dev mode: fromRoute={}, errorType={}, errorMessage={}, errorId={}",
                    fromRoute,
                    errorType,
                    errorMessage,
                    errorId);
            updateUIWithErrorDetails();
            showDevInfo();
        } else {
            // Production: Generic error message only
            LOGGER.warn("Error occurred: fromRoute={}, errorType={}, errorId={}", fromRoute, errorType, errorId);
            updateUIWithGenericError();
        }
    }

    private void createErrorContainer() {
        errorContainer = new VerticalLayout();
        errorContainer.addClassName(CoreConstants.ERROR_CONTAINER_CLASS);
        errorContainer.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        errorContainer.setSpacing(true);
        errorContainer.setAlignItems(Alignment.CENTER);

        title = new H2();
        title.addClassName(CoreConstants.ERROR_VIEW_TITLE_CLASS);

        description = new Span();
        description.addClassName(CoreConstants.ERROR_VIEW_DESCRIPTION_CLASS);

        errorContainer.add(title, description);
        add(errorContainer);
    }

    private void createNavigationButtons() {
        goHome = ButtonHelper.createButton(
                getTranslation(CoreConstants.ERROR_GO_HOME_KEY),
                e -> NavigationHelper.navigateToHome(),
                ButtonVariant.LUMO_PRIMARY);

        tryAgain = ButtonHelper.createButton(
                getTranslation(CoreConstants.ERROR_TRY_AGAIN_KEY),
                e -> NavigationHelper.navigateTo(fromRoute),
                ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(goHome, tryAgain);
        buttons.setSpacing(true);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        errorContainer.add(buttons);
    }

    private void createDevInfoContainer() {
        devContainer = new VerticalLayout();
        devContainer.addClassName(CoreConstants.ERROR_DEV_CONTAINER_CLASS);
        devContainer.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        devContainer.setSpacing(true);
        devContainer.setVisible(false); // Hidden by default

        devTitle = new H3();
        devTitle.addClassName(CoreConstants.ERROR_DEV_TITLE_CLASS);

        Div errorDetails = new Div();
        errorDetails.addClassName(CoreConstants.ERROR_DEV_DETAILS_CLASS);

        errorTypeSpan = new Span();
        errorTypeSpan.addClassName(CoreConstants.ERROR_DEV_TYPE_CLASS);

        errorMessageSpan = new Span();
        errorMessageSpan.addClassName(CoreConstants.ERROR_DEV_MESSAGE_CLASS);

        currentRoute = new Span();
        currentRoute.addClassName(CoreConstants.ERROR_DEV_ROUTE_CLASS);

        timestamp = new Span();
        timestamp.addClassName(CoreConstants.ERROR_DEV_TIMESTAMP_CLASS);

        errorDetails.add(errorTypeSpan, errorMessageSpan, currentRoute);
        devContainer.add(devTitle, errorDetails, timestamp);
        add(devContainer);
    }

    private void extractErrorParameters(final BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParams = location.getQueryParameters();

        fromRoute = queryParams
                .getParameters()
                .getOrDefault(CoreConstants.FROM_PARAM, List.of(""))
                .getFirst();
        errorType = queryParams
                .getParameters()
                .getOrDefault(CoreConstants.ERROR_PARAM, List.of(""))
                .getFirst();
        errorMessage = queryParams
                .getParameters()
                .getOrDefault(CoreConstants.MESSAGE_PARAM, List.of(""))
                .getFirst();
        errorId = queryParams
                .getParameters()
                .getOrDefault(CoreConstants.ID_PARAM, List.of(""))
                .getFirst();
    }

    private boolean shouldRedirectToHome() {
        // Redirect to home if no valid error parameters are provided
        // In production, only 'from' and 'error' parameters are guaranteed
        // In dev, we have 'from', 'error', 'message', and 'id' parameters
        return fromRoute == null || fromRoute.isEmpty() || errorType == null || errorType.isEmpty();
    }

    private void updateUIWithErrorDetails() {
        title.setText(getTranslation(CoreConstants.ERROR_500_KEY));
        description.setText(getTranslation(CoreConstants.ERROR_500_DESCRIPTION_KEY));
        goHome.setText(getTranslation(CoreConstants.MAIN_GO_HOME_KEY));
        tryAgain.setText(getTranslation(CoreConstants.ERROR_TRY_AGAIN_KEY));
    }

    private void updateUIWithGenericError() {
        title.setText(getTranslation(CoreConstants.ERROR_500_KEY));
        description.setText(getTranslation(CoreConstants.ERROR_500_DESCRIPTION_KEY));
        goHome.setText(getTranslation(CoreConstants.MAIN_GO_HOME_KEY));
        tryAgain.setText(getTranslation(CoreConstants.ERROR_TRY_AGAIN_KEY));
        // Hide dev container in production
        devContainer.setVisible(false);
    }

    private void showDevInfo() {
        devTitle.setText(getTranslation(CoreConstants.ERROR_DEV_TITLE_KEY));

        // Sanitize error details for security
        String safeErrorType = sanitizeErrorDetail(errorType);
        String safeErrorMessage = sanitizeErrorDetail(errorMessage);
        String safeErrorId = sanitizeErrorDetail(errorId);

        errorTypeSpan.setText(getTranslation(CoreConstants.ERROR_TYPE_KEY) + ": " + safeErrorType);
        errorMessageSpan.setText(getTranslation(CoreConstants.ERROR_MESSAGE_KEY) + ": " + safeErrorMessage);
        currentRoute.setText(getTranslation(CoreConstants.ERROR_CURRENT_ROUTE_KEY) + ": " + fromRoute);
        timestamp.setText(getTranslation(CoreConstants.ERROR_TIMESTAMP_KEY) + " "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern(CoreConstants.DATETIME_PATTERN)));

        // Add error ID to dev info if available
        if (safeErrorId != null && !safeErrorId.isEmpty()) {
            Span errorIdSpan = new Span();
            errorIdSpan.setText(getTranslation(CoreConstants.ERROR_ID_KEY) + ": " + safeErrorId);
            errorIdSpan.addClassName(CoreConstants.ERROR_DEV_ID_CLASS);
            devContainer.add(errorIdSpan);
        }

        // Only show dev container in dev profile
        devContainer.setVisible(isDevProfile());
    }

    /**
     * Sanitizes error details to prevent XSS and information leakage.
     * Only allows safe characters and limits length.
     *
     * @param errorDetail the error detail to sanitize
     * @return sanitized error detail
     */
    private String sanitizeErrorDetail(final String errorDetail) {
        if (errorDetail == null || errorDetail.isEmpty()) {
            return getTranslation("error.unknown");
        }

        // Remove potentially dangerous characters
        String sanitized =
                errorDetail.replaceAll("[<>\"'&]", "").replaceAll("\\s+", " ").trim();

        // Limit length to prevent information leakage
        if (sanitized.length() > CoreConstants.MAX_ERROR_DETAIL_LENGTH) {
            sanitized = sanitized.substring(0, CoreConstants.MAX_ERROR_DETAIL_LENGTH) + CoreConstants.TRUNCATION_SUFFIX;
        }

        return sanitized.isEmpty() ? getTranslation(CoreConstants.ERROR_UNKNOWN_KEY) : sanitized;
    }

    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains(CoreConstants.DEV_PROFILE);
    }

    @Override
    public String getPageTitle() {
        return getTranslation(CoreConstants.ERROR_500_KEY);
    }
}
