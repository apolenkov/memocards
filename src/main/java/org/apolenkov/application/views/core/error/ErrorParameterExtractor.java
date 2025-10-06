package org.apolenkov.application.views.core.error;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import java.util.List;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for extracting error parameters from routing events.
 * Handles the extraction and logging of error-related query parameters.
 */
public final class ErrorParameterExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorParameterExtractor.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private ErrorParameterExtractor() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Extracts error parameters from the before enter event and populates the state.
     *
     * @param event the before enter event containing query parameters
     * @param state the error view state to populate
     */
    public static void extractParameters(final BeforeEnterEvent event, final ErrorViewState state) {
        Location location = event.getLocation();
        QueryParameters queryParams = location.getQueryParameters();

        String fromRoute = extractParameter(queryParams, CoreConstants.FROM_PARAM);
        String errorType = extractParameter(queryParams, CoreConstants.ERROR_PARAM);
        String errorMessage = extractParameter(queryParams, CoreConstants.MESSAGE_PARAM);
        String errorId = extractParameter(queryParams, CoreConstants.ID_PARAM);

        state.updateState(fromRoute, errorType, errorMessage, errorId);

        logExtractedParameters(state);
    }

    /**
     * Extracts a single parameter value from query parameters.
     *
     * @param queryParams the query parameters
     * @param paramName the parameter name to extract
     * @return the parameter value or empty string if not found
     */
    private static String extractParameter(final QueryParameters queryParams, final String paramName) {
        return queryParams.getParameters().getOrDefault(paramName, List.of("")).getFirst();
    }

    /**
     * Logs the extracted error parameters for debugging purposes.
     *
     * @param state the error view state containing the parameters
     */
    private static void logExtractedParameters(final ErrorViewState state) {
        LOGGER.info(
                "Extracted parameters: fromRoute={}, errorType={}, errorMessage={}, errorId={}",
                state.getFromRoute(),
                state.getErrorType(),
                state.getErrorMessage(),
                state.getErrorId());
    }
}
