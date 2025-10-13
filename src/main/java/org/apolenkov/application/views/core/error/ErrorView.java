package org.apolenkov.application.views.core.error;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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

    // ==================== Fields ====================

    private final transient Environment environment;
    private final transient ErrorDevInfo devInfo;

    // Error state
    private transient ErrorState errorState;

    // UI Components
    private H2 errorTitle;
    private Span errorDescription;
    private VerticalLayout errorContainer;

    // ==================== Constructor ====================

    /**
     * Creates a new error view.
     *
     * @param env Spring environment for profile detection
     */
    public ErrorView(final Environment env) {
        this.environment = env;
        this.devInfo = new ErrorDevInfo(env);
        this.errorState = new ErrorState("", "", "", "");
    }

    // ==================== Lifecycle & Initialization ====================

    @PostConstruct
    @SuppressWarnings("unused")
    private void initializeUI() {
        LOGGER.debug("Initializing ErrorView UI components");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        createErrorLayout();
        add(errorContainer, devInfo);

        LOGGER.debug("ErrorView UI initialization completed");
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        LOGGER.debug("Processing beforeEnter event for ErrorView");

        extractErrorParameters(event);

        if (!hasValidErrorParameters()) {
            handleRedirectToHome(event);
            return;
        }

        updateErrorDisplay();
        addNavigationButtons();
        processErrorBasedOnProfile();
    }

    /**
     * Creates the main error layout.
     */
    private void createErrorLayout() {
        errorContainer = new VerticalLayout();
        errorContainer.addClassName(CoreConstants.ERROR_CONTAINER_CLASS);
        errorContainer.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        errorContainer.setSpacing(true);
        errorContainer.setAlignItems(FlexComponent.Alignment.CENTER);

        errorTitle = new H2();
        errorTitle.addClassName(CoreConstants.ERROR_VIEW_TITLE_CLASS);

        errorDescription = new Span();
        errorDescription.addClassName(CoreConstants.ERROR_VIEW_DESCRIPTION_CLASS);

        errorContainer.add(errorTitle, errorDescription);
    }

    /**
     * Extracts error parameters from the before enter event.
     *
     * @param event the before enter event containing query parameters
     */
    private void extractErrorParameters(final BeforeEnterEvent event) {
        Location location = event.getLocation();
        QueryParameters queryParams = location.getQueryParameters();

        String fromRoute = extractParameter(queryParams, CoreConstants.FROM_PARAM);
        String errorType = extractParameter(queryParams, CoreConstants.ERROR_PARAM);
        String errorMessage = extractParameter(queryParams, CoreConstants.MESSAGE_PARAM);
        String errorId = extractParameter(queryParams, CoreConstants.ID_PARAM);

        errorState = new ErrorState(fromRoute, errorType, errorMessage, errorId);

        LOGGER.info(
                "Extracted parameters: fromRoute={}, errorType={}, errorMessage={}, errorId={}",
                errorState.fromRoute(),
                errorState.errorType(),
                errorState.errorMessage(),
                errorState.errorId());
    }

    /**
     * Extracts a single parameter value from query parameters.
     *
     * @param queryParams the query parameters
     * @param paramName the parameter name to extract
     * @return the parameter value or empty string if not found
     */
    private String extractParameter(final QueryParameters queryParams, final String paramName) {
        return queryParams.getParameters().getOrDefault(paramName, List.of("")).getFirst();
    }

    /**
     * Checks if error parameters are valid.
     *
     * @return true if valid
     */
    private boolean hasValidErrorParameters() {
        return !errorState.fromRoute().isEmpty() && !errorState.errorType().isEmpty();
    }

    /**
     * Updates the error display with error information.
     */
    private void updateErrorDisplay() {
        errorTitle.setText(getTranslation(CoreConstants.ERROR_500_KEY));
        errorDescription.setText(getTranslation(CoreConstants.ERROR_500_DESCRIPTION_KEY));
    }

    /**
     * Adds navigation buttons to the error container.
     */
    private void addNavigationButtons() {
        HorizontalLayout navigationButtons = createNavigationButtons();
        errorContainer.add(navigationButtons);
    }

    /**
     * Creates navigation buttons for error recovery.
     *
     * @return configured button layout
     */
    private HorizontalLayout createNavigationButtons() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button goHomeButton = ButtonHelper.createButton(
                getTranslation(CoreConstants.ERROR_GO_HOME_KEY),
                e -> NavigationHelper.navigateToHome(),
                ButtonVariant.LUMO_PRIMARY);

        Button tryAgainButton = ButtonHelper.createButton(
                getTranslation(CoreConstants.ERROR_TRY_AGAIN_KEY),
                e -> NavigationHelper.navigateTo(errorState.fromRoute()),
                ButtonVariant.LUMO_TERTIARY);

        layout.add(goHomeButton, tryAgainButton);
        return layout;
    }

    /**
     * Handles redirect to home page when no valid error parameters are found.
     *
     * @param event the before enter event for redirection
     */
    private void handleRedirectToHome(final BeforeEnterEvent event) {
        LOGGER.info("No valid error parameters found, redirecting to home page");
        NavigationHelper.forwardToHome(event);
    }

    /**
     * Processes error display based on the current profile (dev vs production).
     */
    private void processErrorBasedOnProfile() {
        if (isDevProfile()) {
            processDevProfileError();
        } else {
            processProductionError();
        }
    }

    /**
     * Processes error in development profile with detailed information.
     */
    private void processDevProfileError() {
        LOGGER.info("Processing error in dev mode: {}", errorState);
        devInfo.updateDevInfo(errorState);
    }

    /**
     * Processes error in production profile with generic information only.
     */
    private void processProductionError() {
        LOGGER.warn(
                "Error occurred: fromRoute={}, errorType={}, errorId={}",
                errorState.fromRoute(),
                errorState.errorType(),
                errorState.errorId());
        devInfo.hideDevInfo();
    }

    /**
     * Checks if the current profile is development.
     *
     * @return true if in development profile
     */
    private boolean isDevProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains(CoreConstants.DEV_PROFILE);
    }

    @Override
    public String getPageTitle() {
        return getTranslation(CoreConstants.ERROR_500_KEY);
    }

    /**
     * Error state record for encapsulating error parameters.
     *
     * @param fromRoute the route that caused the error
     * @param errorType the type of error
     * @param errorMessage the error message
     * @param errorId the unique error identifier
     */
    public record ErrorState(String fromRoute, String errorType, String errorMessage, String errorId) {}
}
