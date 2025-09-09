package org.apolenkov.application.exceptions;

/**
 * Exception thrown when an entity is not found.
 * This exception can be caught by Vaadin's error handling system to display
 * a user-friendly error page automatically.
 */
public class EntityNotFoundException extends RuntimeException {

    private final String entityId;
    private final String backRoute;

    /**
     * Creates a new EntityNotFoundException with custom message.
     *
     * @param entityIdParam the ID of the entity that was not found
     * @param backRouteParam the route to navigate back to
     * @param message custom error message
     */
    public EntityNotFoundException(final String entityIdParam, final String backRouteParam, final String message) {
        super(message);
        this.entityId = entityIdParam;
        this.backRoute = backRouteParam;
    }

    /**
     * Gets the entity ID.
     *
     * @return the entity ID
     */
    public String getEntityId() {
        return entityId;
    }

    /**
     * Gets the back route.
     *
     * @return the back route
     */
    public String getBackRoute() {
        return backRoute;
    }
}
