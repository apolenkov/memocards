package org.apolenkov.application.utils;

import org.apolenkov.application.exceptions.EntityNotFoundException;

/**
 * Utility class for throwing EntityNotFoundException in a convenient way.
 * Provides static methods for common entity types.
 */
public final class EntityErrorUtils {

    private EntityErrorUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Throws EntityNotFoundException with custom message.
     *
     * @param entityId the ID of the entity that was not found
     * @param backRoute the route to navigate back to
     * @param message custom error message
     * @throws EntityNotFoundException always
     */
    public static void throwEntityNotFound(final String entityId, final String backRoute, final String message) {
        throw new EntityNotFoundException(entityId, backRoute, message);
    }
}
