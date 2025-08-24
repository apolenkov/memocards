package org.apolenkov.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.server.HttpStatusCode;

/**
 * Error handler for page not found errors (HTTP 404).
 * Redirects users to the home page when they access non-existent routes.
 */
@ParentLayout(PublicLayout.class)
public class NotFoundView extends Div implements HasErrorParameter<NotFoundException> {

    /**
     * Handles the error by redirecting to home page.
     *
     * @param event navigation event
     * @param parameter error parameter with exception details
     * @return HTTP 404 status code
     */
    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        event.rerouteTo("");
        return HttpStatusCode.NOT_FOUND.getCode();
    }
}
