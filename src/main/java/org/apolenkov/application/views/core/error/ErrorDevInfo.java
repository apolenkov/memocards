package org.apolenkov.application.views.core.error;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.shared.utils.SanitizationUtils;
import org.springframework.core.env.Environment;

/**
 * Component responsible for displaying development-specific error information.
 * Shows detailed error information only in development profile for debugging purposes.
 */
public final class ErrorDevInfo extends Composite<Component> {

    private final VerticalLayout devContainer;
    private final H3 devTitle;
    private final Span errorTypeSpan;
    private final Span errorMessageSpan;
    private final Span currentRouteSpan;
    private final Span timestampSpan;
    private final transient Environment environment;

    /**
     * Creates a new ErrorDevInfo component.
     *
     * @param env Spring environment for profile detection
     */
    public ErrorDevInfo(final Environment env) {
        this.environment = env;
        this.devContainer = createDevContainer();
        this.devTitle = createDevTitle();
        this.errorTypeSpan = createErrorTypeSpan();
        this.errorMessageSpan = createErrorMessageSpan();
        this.currentRouteSpan = createCurrentRouteSpan();
        this.timestampSpan = createTimestampSpan();

        setupDevContainer();
    }

    @Override
    protected VerticalLayout initContent() {
        return devContainer;
    }

    /**
     * Creates the main development container.
     *
     * @return configured dev container
     */
    private VerticalLayout createDevContainer() {
        VerticalLayout container = new VerticalLayout();
        container.addClassName(CoreConstants.ERROR_DEV_CONTAINER_CLASS);
        container.addClassName(CoreConstants.SURFACE_PANEL_CLASS);
        container.setSpacing(true);
        container.setVisible(false); // Hidden by default
        return container;
    }

    /**
     * Creates the development title component.
     *
     * @return configured dev title
     */
    private H3 createDevTitle() {
        H3 title = new H3();
        title.addClassName(CoreConstants.ERROR_DEV_TITLE_CLASS);
        return title;
    }

    /**
     * Creates the error type span component.
     *
     * @return configured error type span
     */
    private Span createErrorTypeSpan() {
        Span span = new Span();
        span.addClassName(CoreConstants.ERROR_DEV_TYPE_CLASS);
        return span;
    }

    /**
     * Creates the error message span component.
     *
     * @return configured error message span
     */
    private Span createErrorMessageSpan() {
        Span span = new Span();
        span.addClassName(CoreConstants.ERROR_DEV_MESSAGE_CLASS);
        return span;
    }

    /**
     * Creates the current route span component.
     *
     * @return configured current route span
     */
    private Span createCurrentRouteSpan() {
        Span span = new Span();
        span.addClassName(CoreConstants.ERROR_DEV_ROUTE_CLASS);
        return span;
    }

    /**
     * Creates the timestamp span component.
     *
     * @return configured timestamp span
     */
    private Span createTimestampSpan() {
        Span span = new Span();
        span.addClassName(CoreConstants.ERROR_DEV_TIMESTAMP_CLASS);
        return span;
    }

    /**
     * Sets up the development container with all components.
     */
    private void setupDevContainer() {
        Div errorDetails = new Div();
        errorDetails.addClassName(CoreConstants.ERROR_DEV_DETAILS_CLASS);
        errorDetails.add(errorTypeSpan, errorMessageSpan, currentRouteSpan);

        devContainer.add(devTitle, errorDetails, timestampSpan);
    }

    /**
     * Updates the development info with error details and shows it if in dev profile.
     *
     * @param state the error view state containing error information
     */
    public void updateDevInfo(final ErrorViewState state) {
        if (!isDevProfile()) {
            devContainer.setVisible(false);
            return;
        }

        devTitle.setText(getTranslation(CoreConstants.ERROR_DEV_TITLE_KEY));

        // Sanitize error details for security
        String unknownText = getTranslation(CoreConstants.ERROR_UNKNOWN_KEY);
        String safeErrorType = SanitizationUtils.sanitizeErrorDetail(state.getErrorType(), unknownText);
        String safeErrorMessage = SanitizationUtils.sanitizeErrorDetail(state.getErrorMessage(), unknownText);
        String safeErrorId = SanitizationUtils.sanitizeErrorDetail(state.getErrorId(), unknownText);

        errorTypeSpan.setText(getTranslation(CoreConstants.ERROR_TYPE_KEY) + ": " + safeErrorType);
        errorMessageSpan.setText(getTranslation(CoreConstants.ERROR_MESSAGE_KEY) + ": " + safeErrorMessage);
        currentRouteSpan.setText(getTranslation(CoreConstants.ERROR_CURRENT_ROUTE_KEY) + ": " + state.getFromRoute());
        timestampSpan.setText(getTranslation(CoreConstants.ERROR_TIMESTAMP_KEY) + " "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern(CoreConstants.DATETIME_PATTERN)));

        // Add error ID to dev info if available
        if (safeErrorId != null && !safeErrorId.isEmpty()) {
            addErrorIdSpan(safeErrorId);
        }

        devContainer.setVisible(true);
    }

    /**
     * Adds an error ID span to the development container.
     *
     * @param safeErrorId the sanitized error ID
     */
    private void addErrorIdSpan(final String safeErrorId) {
        Span errorIdSpan = new Span();
        errorIdSpan.setText(getTranslation(CoreConstants.ERROR_ID_KEY) + ": " + safeErrorId);
        errorIdSpan.addClassName(CoreConstants.ERROR_DEV_ID_CLASS);
        devContainer.add(errorIdSpan);
    }

    /**
     * Hides the development info container.
     */
    public void hideDevInfo() {
        devContainer.setVisible(false);
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
}
