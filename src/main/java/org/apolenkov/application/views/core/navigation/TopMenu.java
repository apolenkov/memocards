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

    // UI constants
    private static final String EMPTY_STRING = "";

    private Anchor title;

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

        setupTitle();
        initializeServices();
        refreshMenu();
    }

    /**
     * Sets up the title anchor with logo icon.
     */
    private void setupTitle() {
        title = new Anchor(RouteConstants.ROOT_PATH, EMPTY_STRING);

        Image navIcon = new Image(
                new StreamResource(VaadinApplicationShell.ResourcePaths.LOGO_ICON_NAME, () -> getClass()
                        .getResourceAsStream(VaadinApplicationShell.ResourcePaths.LOGO_ICON_FULL_PATH)),
                getTranslation(CoreConstants.APP_TITLE_KEY));
        title.add(navIcon);
    }

    /**
     * Initializes the layout service with menu buttons.
     */
    private void initializeServices() {
        layoutService.initializeMenuButtons(
                getTranslation(CoreConstants.MAIN_DECKS_KEY),
                getTranslation(CoreConstants.MAIN_STATS_KEY),
                getTranslation(CoreConstants.MAIN_SETTINGS_KEY),
                getTranslation(CoreConstants.ADMIN_CONTENT_TITLE_KEY),
                getTranslation(CoreConstants.MAIN_LOGOUT_KEY));
    }

    /**
     * Refreshes the entire menu content based on current authentication state.
     * This method is called after route changes to ensure the menu accurately
     * reflects the user's current authentication status. Delegates to layout service
     * for UI component creation and coordination.
     */
    public void refreshMenu() {
        removeAll();

        Authentication auth = authService.getCurrentAuthentication();
        boolean isAuthenticated = authService.isAuthenticated(auth);

        HorizontalLayout leftSection = layoutService.createLeftSection(
                title, auth, isAuthenticated, getTranslation(CoreConstants.MAIN_GREETING_KEY));
        HorizontalLayout buttonsSection = layoutService.createMenuButtonsLayout(auth, isAuthenticated);

        add(leftSection);
        add(buttonsSection);
    }
}
