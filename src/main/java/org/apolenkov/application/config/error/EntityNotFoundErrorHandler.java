package org.apolenkov.application.config.error;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;
import org.apolenkov.application.views.core.error.EntityNotFoundError;
import org.apolenkov.application.views.core.exception.EntityNotFoundException;
import org.apolenkov.application.views.core.layout.PublicLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global error handler for EntityNotFoundException.
 * Automatically displays a user-friendly error page when any EntityNotFoundException is thrown.
 */
@Route(value = "error/entity-not-found", layout = PublicLayout.class)
@AnonymousAllowed
public class EntityNotFoundErrorHandler extends VerticalLayout implements HasErrorParameter<EntityNotFoundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityNotFoundErrorHandler.class);

    /**
     * Creates a new EntityNotFoundErrorHandler.
     */
    public EntityNotFoundErrorHandler() {
        // Constructor for dependency injection
    }

    /**
     * Handles the EntityNotFoundException by displaying a custom error page.
     *
     * @param event the BeforeEnterEvent that triggered the error
     * @param parameter the ErrorParameter containing the EntityNotFoundException
     * @return the HTTP status code for "Not Found" (404)
     */
    @Override
    public int setErrorParameter(
            final BeforeEnterEvent event, final ErrorParameter<EntityNotFoundException> parameter) {
        EntityNotFoundException exception = parameter.getException();

        LOGGER.warn(
                "Entity not found with ID '{}', redirecting to back route: {}",
                exception.getEntityId(),
                exception.getBackRoute());

        // Create and display the error component
        EntityNotFoundError errorComponent =
                new EntityNotFoundError(exception.getEntityId(), exception.getBackRoute(), exception.getMessage());

        // Clear existing content and add error component
        removeAll();
        add(errorComponent);

        return HttpServletResponse.SC_NOT_FOUND;
    }
}
