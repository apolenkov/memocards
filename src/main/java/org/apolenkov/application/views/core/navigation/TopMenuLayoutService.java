package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Service responsible for creating and managing UI layouts for the top menu.
 * Handles the creation of menu sections, user greetings, and menu button layouts.
 */
@Component
@UIScope
public class TopMenuLayoutService {

    // Logout button constants
    private static final String LOGOUT_ROUTE = RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE;

    // Navigation test IDs
    private static final String NAV_DECKS_TEST_ID = "nav-decks";
    private static final String NAV_STATS_TEST_ID = "nav-stats";
    private static final String NAV_SETTINGS_TEST_ID = "nav-settings";
    private static final String NAV_ADMIN_CONTENT_TEST_ID = "nav-admin-content";
    private static final String LOGOUT_TEST_ID = "nav-logout";

    private final TopMenuAuthService authService;
    private final TopMenuButtonFactory buttonFactory;

    private final List<MenuButton> menuButtons = new ArrayList<>();

    /**
     * Creates a new TopMenuLayoutService with required dependencies.
     *
     * @param authenticationService service for authentication operations
     * @param buttonFactoryService factory for creating menu buttons
     */
    public TopMenuLayoutService(
            final TopMenuAuthService authenticationService, final TopMenuButtonFactory buttonFactoryService) {
        this.authService = authenticationService;
        this.buttonFactory = buttonFactoryService;
    }

    /**
     * Initializes the menu button configuration for different user roles.
     * Creates menu buttons with appropriate role requirements and navigation targets.
     *
     * @param translationProvider function to get translations for button text
     */
    public void initializeMenuButtons(final UnaryOperator<String> translationProvider) {
        menuButtons.clear();
        menuButtons.add(new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_DECKS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.DECKS_ROUTE,
                NAV_DECKS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_STATS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.STATS_ROUTE,
                NAV_STATS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_SETTINGS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.SETTINGS_ROUTE,
                NAV_SETTINGS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                translationProvider.apply(CoreConstants.ADMIN_CONTENT_TITLE_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.ADMIN_CONTENT_ROUTE,
                NAV_ADMIN_CONTENT_TEST_ID,
                false,
                SecurityConstants.ROLE_ADMIN));
        menuButtons.add(new MenuButton(
                translationProvider.apply(CoreConstants.MAIN_LOGOUT_KEY), LOGOUT_ROUTE, LOGOUT_TEST_ID, false));
    }

    /**
     * Creates the left section of the menu containing title and user greeting.
     *
     * @param title the title anchor component
     * @param auth the cached authentication context
     * @param isAuthenticated the cached authentication status
     * @param translationProvider function to get translations for greeting text
     * @return the configured left section layout
     */
    public HorizontalLayout createLeftSection(
            final Anchor title,
            final Authentication auth,
            final boolean isAuthenticated,
            final UnaryOperator<String> translationProvider) {
        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(FlexComponent.Alignment.CENTER);
        left.setSpacing(true);
        left.add(title);

        if (isAuthenticated) {
            Div greeting = createUserGreeting(auth, translationProvider);
            left.add(greeting);
        }

        return left;
    }

    /**
     * Creates the horizontal layout containing all visible menu buttons.
     * Filters buttons based on user authentication and role requirements,
     * then creates and configures each button with appropriate styling and
     * click handlers.
     *
     * @param auth the cached authentication context
     * @param isAuthenticated the cached authentication status
     * @param translationProvider function to get translations for button text
     * @return a horizontal layout containing the filtered menu buttons
     */
    public HorizontalLayout createMenuButtonsLayout(
            final Authentication auth, final boolean isAuthenticated, final UnaryOperator<String> translationProvider) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        for (MenuButton menuButton : menuButtons) {
            if (authService.shouldShowButton(menuButton, auth, isAuthenticated)) {
                Button button = buttonFactory.createButton(menuButton, translationProvider);
                buttonsLayout.add(button);
            }
        }

        return buttonsLayout;
    }

    /**
     * Creates the user greeting component.
     *
     * @param auth the authentication context
     * @param translationProvider function to get translations for greeting text
     * @return the configured greeting div
     */
    private Div createUserGreeting(final Authentication auth, final UnaryOperator<String> translationProvider) {
        String displayName = authService.getUserDisplayName(auth);

        Div greeting = new Div();
        greeting.setText(
                translationProvider.apply(CoreConstants.MAIN_GREETING_KEY).replace("{0}", displayName));
        greeting.addClassName(CoreConstants.TOP_MENU_GREETING_CLASS);

        return greeting;
    }
}
