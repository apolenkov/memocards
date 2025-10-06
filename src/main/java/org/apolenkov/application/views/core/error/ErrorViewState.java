package org.apolenkov.application.views.core.error;

/**
 * State management for ErrorView component.
 * Encapsulates all error-related data and provides methods for state validation.
 */
public final class ErrorViewState {

    private String fromRoute;
    private String errorType;
    private String errorMessage;
    private String errorId;

    /**
     * Creates a new ErrorViewState with empty values.
     */
    public ErrorViewState() {
        this.fromRoute = "";
        this.errorType = "";
        this.errorMessage = "";
        this.errorId = "";
    }

    /**
     * Gets the route that caused the error.
     *
     * @return the route
     */
    public String getFromRoute() {
        return fromRoute;
    }

    /**
     * Gets the error type.
     *
     * @return the error type
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the error ID.
     *
     * @return the error ID
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * Checks if the state contains valid error parameters.
     * Valid parameters require at least fromRoute and errorType to be non-empty.
     *
     * @return true if the state has valid error parameters
     */
    public boolean hasValidErrorParameters() {
        return !fromRoute.isEmpty() && !errorType.isEmpty();
    }

    /**
     * Updates all state values at once.
     *
     * @param fromRouteValue the route
     * @param errorTypeValue the error type
     * @param errorMessageValue the error message
     * @param errorIdValue the error ID
     */
    public void updateState(
            final String fromRouteValue,
            final String errorTypeValue,
            final String errorMessageValue,
            final String errorIdValue) {
        this.fromRoute = fromRouteValue != null ? fromRouteValue : "";
        this.errorType = errorTypeValue != null ? errorTypeValue : "";
        this.errorMessage = errorMessageValue != null ? errorMessageValue : "";
        this.errorId = errorIdValue != null ? errorIdValue : "";
    }

    @Override
    public String toString() {
        return String.format(
                "ErrorViewState{fromRoute='%s', errorType='%s', errorMessage='%s', errorId='%s'}",
                fromRoute, errorType, errorMessage, errorId);
    }
}
