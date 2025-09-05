package org.apolenkov.application.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;

/**
 * View component for displaying access denied (403 Forbidden) errors.
 * Shows user-friendly error message for unauthorized access attempts.
 */
@Route(value = "access-denied", layout = PublicLayout.class)
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout implements HasDynamicTitle {

    /**
     * Creates AccessDeniedView with localized error heading and simple layout.
     */
    public AccessDeniedView() {
        // Constructor only - no UI initialization here
    }

    /**
     * Initializes the view components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    private void init() {
        add(new H2(getTranslation("error.403")));
    }

    /**
     * Gets localized page title for access denied view.
     *
     * @return the localized page title for the access denied view
     */
    @Override
    public String getPageTitle() {
        return getTranslation("error.403");
    }
}
