package org.apolenkov.application.views.core.error;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Generic error page view with user-friendly error display.
 * Coordinates between specialized components to provide a complete error handling experience.
 * Shows a formatted error message with navigation options.
 * In development profile, displays additional debugging information.
 */
@Route(value = RouteConstants.ERROR_ROUTE, layout = PublicLayout.class)
@AnonymousAllowed
public final class ErrorView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorView.class);
    private final transient Environment environment;

    // Specialized components
    private final transient ErrorViewState state;
    private final transient ErrorViewLayout layout;
    private final transient ErrorDevInfo devInfo;

    /**
     * Creates a new error view.
     *
     * @param env Spring environment for profile detection
     */
    public ErrorView(final Environment env) {
        this.environment = env;
        this.state = new ErrorViewState();
        this.layout = new ErrorViewLayout();
        this.devInfo = new ErrorDevInfo(env);
    }

    @PostConstruct
    private void initializeUI() {
        LOGGER.debug("Initializing ErrorView UI components");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        setupLayout();

        LOGGER.debug("ErrorView UI initialization completed");
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {
        LOGGER.debug("Processing beforeEnter event for ErrorView");

        ErrorParameterExtractor.extractParameters(event, state);

        if (!state.hasValidErrorParameters()) {
            handleRedirectToHome(event);
            return;
        }

        createNavigationButtons();
        processErrorBasedOnProfile();
    }

    /**
     * Sets up the main layout with all components.
     */
    private void setupLayout() {
        add(layout);
        add(devInfo);
    }

    /**
     * Creates navigation buttons with the correct fromRoute from state.
     */
    private void createNavigationButtons() {
        ErrorNavigationButtons navigationButtons = new ErrorNavigationButtons(state.getFromRoute());
        layout.addComponent(navigationButtons);
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
        LOGGER.info("Processing error in dev mode: {}", state);
        layout.updateWithGenericError();
        devInfo.updateDevInfo(state);
    }

    /**
     * Processes error in production profile with generic information only.
     */
    private void processProductionError() {
        LOGGER.warn(
                "Error occurred: fromRoute={}, errorType={}, errorId={}",
                state.getFromRoute(),
                state.getErrorType(),
                state.getErrorId());
        layout.updateWithGenericError();
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
}
