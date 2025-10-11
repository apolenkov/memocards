package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.vaadin.VaadinApplicationShell;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Top navigation menu component for the application.
 * Coordinates between authentication, layout, and button services to provide
 * a complete navigation interface with user authentication status and role-based menu items.
 */
@Component
@UIScope
public class TopMenu extends HorizontalLayout {

    private final transient TopMenuAuthService authService;
    private final transient TopMenuLayoutService layoutService;

    /**
     * Creates a new TopMenu with required service dependencies.
     *
     * @param authenticationService service for authentication operations
     * @param layoutServiceComponent service for UI layout operations
     */
    public TopMenu(final TopMenuAuthService authenticationService, final TopMenuLayoutService layoutServiceComponent) {
        this.authService = authenticationService;
        this.layoutService = layoutServiceComponent;
    }

    /**
     * Initializes the menu components after dependency injection is complete.
     * This method is called after the constructor and ensures that all
     * dependencies are properly injected before UI initialization.
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);

        refreshMenu();
    }

    /**
     * Creates a new title anchor with logo icon.
     * Each call creates a fresh instance to avoid component reuse.
     *
     * @return new Anchor instance with logo
     */
    private Anchor createTitle() {
        Anchor anchor = new Anchor(RouteConstants.ROOT_PATH, "");

        Image navIcon = new Image(
                new StreamResource(VaadinApplicationShell.ResourcePaths.LOGO_ICON_NAME, () -> getClass()
                        .getResourceAsStream(VaadinApplicationShell.ResourcePaths.LOGO_ICON_FULL_PATH)),
                getTranslation(CoreConstants.APP_TITLE_KEY));
        anchor.add(navIcon);
        return anchor;
    }

    /**
     * Refreshes the entire menu content based on current authentication state.
     * This method is called after route changes to ensure the menu accurately
     * reflects the user's current authentication status. Delegates to layout service
     * for UI component creation and coordination.
     * Creates fresh component instances to avoid state tree corruption.
     */
    public void refreshMenu() {
        removeAll();

        Authentication auth = authService.getCurrentAuthentication();
        boolean isAuthenticated = authService.isAuthenticated(auth);

        // Create fresh title anchor each time to avoid component reuse
        Anchor freshTitle = createTitle();

        HorizontalLayout leftSection = layoutService.createLeftSection(
                freshTitle, auth, isAuthenticated, getTranslation(CoreConstants.MAIN_GREETING_KEY));

        HorizontalLayout buttonsSection = layoutService.createMenuButtonsLayout(auth, isAuthenticated);

        add(leftSection);
        add(buttonsSection);
    }
}
