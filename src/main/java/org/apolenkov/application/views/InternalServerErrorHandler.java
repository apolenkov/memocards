package org.apolenkov.application.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.server.HttpStatusCode;

/**
 * Error handler for internal server errors (HTTP 500).
 * Redirects users to a user-friendly error page when server exceptions occur.
 */
@ParentLayout(PublicLayout.class)
public class InternalServerErrorHandler extends Div implements HasErrorParameter<Exception> {

    /**
     * Handles the error by redirecting to error page.
     *
     * @param event navigation event
     * @param parameter error parameter with exception details
     * @return HTTP 500 status code
     */
    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
        event.rerouteTo("error");
        return HttpStatusCode.INTERNAL_SERVER_ERROR.getCode();
    }
}
