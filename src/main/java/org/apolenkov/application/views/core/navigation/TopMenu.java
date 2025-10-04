package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.config.vaadin.VaadinApplicationShell;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.practice.components.PracticeSettingsDialog;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

/**
 * Top navigation menu component for the application.
 * Provides the main navigation interface including user authentication
 * status, role-based menu items, and navigation controls.
 */
@Component
@UIScope
public class TopMenu extends HorizontalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopMenu.class);

    // Logout button constants
    private static final String LOGOUT_ROUTE = RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE;

    // Navigation test IDs
    private static final String NAV_DECKS_TEST_ID = "nav-decks";
    private static final String NAV_STATS_TEST_ID = "nav-stats";
    private static final String NAV_SETTINGS_TEST_ID = "nav-settings";
    private static final String NAV_ADMIN_CONTENT_TEST_ID = "nav-admin-content";
    private static final String LOGOUT_TEST_ID = "nav-logout";

    // UI constants
    private static final String EMPTY_STRING = "";

    private final List<MenuButton> menuButtons = new ArrayList<>();
    private Anchor title;

    private final transient UserUseCase userUseCase;
    private final transient PracticeSettingsService practiceSettingsService;

    /**
     * Creates a new TopMenu with required dependencies.
     *
     * @param useCase service for user operations and current user information
     * @param settingsService service for practice session configuration
     */
    public TopMenu(final UserUseCase useCase, final PracticeSettingsService settingsService) {
        this.userUseCase = useCase;
        this.practiceSettingsService = settingsService;
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

        title = new Anchor(RouteConstants.ROOT_PATH, EMPTY_STRING);

        Image navIcon = new Image(
                new StreamResource(VaadinApplicationShell.ResourcePaths.LOGO_ICON_NAME, () -> getClass()
                        .getResourceAsStream(VaadinApplicationShell.ResourcePaths.LOGO_ICON_FULL_PATH)),
                getTranslation(CoreConstants.APP_TITLE_KEY));
        title.add(navIcon);

        initializeMenuButtons();
        refreshMenu();
    }

    /**
     * Initializes the menu button configuration for different user roles.
     * Creates menu buttons with appropriate role requirements and navigation targets.
     */
    private void initializeMenuButtons() {
        menuButtons.add(new MenuButton(
                getTranslation(CoreConstants.MAIN_DECKS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.DECKS_ROUTE,
                NAV_DECKS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation(CoreConstants.MAIN_STATS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.STATS_ROUTE,
                NAV_STATS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation(CoreConstants.MAIN_SETTINGS_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.SETTINGS_ROUTE,
                NAV_SETTINGS_TEST_ID,
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation(CoreConstants.ADMIN_CONTENT_TITLE_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.ADMIN_CONTENT_ROUTE,
                NAV_ADMIN_CONTENT_TEST_ID,
                false,
                SecurityConstants.ROLE_ADMIN));
        menuButtons.add(
                new MenuButton(getTranslation(CoreConstants.MAIN_LOGOUT_KEY), LOGOUT_ROUTE, LOGOUT_TEST_ID, false));
    }

    /**
     * Creates the horizontal layout containing all visible menu buttons.
     * Filters buttons based on user authentication and role requirements,
     * then creates and configures each button with appropriate styling and
     * click handlers.
     *
     * @param auth the cached authentication context
     * @param isAuthenticated the cached authentication status
     * @return a horizontal layout containing the filtered menu buttons
     */
    private HorizontalLayout createMenuButtonsLayout(final Authentication auth, final boolean isAuthenticated) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        for (MenuButton menuButton : menuButtons) {
            if (shouldShowButton(menuButton, auth, isAuthenticated)) {
                Button button = createButton(menuButton);
                buttonsLayout.add(button);
            }
        }

        return buttonsLayout;
    }

    /**
     * Refreshes the entire menu content based on current authentication state.
     * This method is called after route changes to ensure the menu accurately
     * reflects the user's current authentication status. It rebuilds the greeting
     * section and button layout to match the current user context.
     */
    public void refreshMenu() {
        removeAll();

        // Cache authentication context to avoid multiple calls
        Authentication auth = getCurrentAuthentication();
        boolean isAuthenticated = isAuthenticated(auth);

        HorizontalLayout leftSection = createLeftSection(auth, isAuthenticated);
        HorizontalLayout buttonsSection = createMenuButtonsLayout(auth, isAuthenticated);

        add(leftSection);
        add(buttonsSection);
    }

    /**
     * Creates the left section of the menu containing title and user greeting.
     *
     * @param auth the cached authentication context
     * @param isAuthenticated the cached authentication status
     * @return the configured left section layout
     */
    private HorizontalLayout createLeftSection(final Authentication auth, final boolean isAuthenticated) {
        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(Alignment.CENTER);
        left.setSpacing(true);
        left.add(title);

        if (isAuthenticated) {
            Div greeting = createUserGreeting(auth);
            left.add(greeting);
        }

        return left;
    }

    /**
     * Gets the current authentication context.
     *
     * @return the current authentication or null
     */
    private Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Checks if the user is authenticated.
     *
     * @param auth the authentication context
     * @return true if user is authenticated
     */
    private boolean isAuthenticated(final Authentication auth) {
        return auth != null && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * Creates the user greeting component.
     *
     * @param auth the authentication context
     * @return the configured greeting div
     */
    private Div createUserGreeting(final Authentication auth) {
        String displayName = getUserDisplayName(auth);

        Div greeting = new Div();
        greeting.setText(getTranslation(CoreConstants.MAIN_GREETING_KEY, displayName));
        greeting.addClassName(CoreConstants.TOP_MENU_GREETING_CLASS);

        return greeting;
    }

    /**
     * Gets the user's display name, falling back to authentication name if needed.
     *
     * @param auth the authentication context
     * @return the user's display name
     */
    private String getUserDisplayName(final Authentication auth) {
        String authName = auth.getName();

        try {
            String displayName = userUseCase.getCurrentUser().getName();
            return (displayName == null || displayName.isBlank()) ? authName : displayName;
        } catch (AuthenticationException e) {
            LOGGER.warn("Failed to get user display name due to authentication issue: {}", e.getMessage());
            return authName;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to get user display name due to database issue: {}", e.getMessage());
            return authName;
        } catch (Exception e) {
            LOGGER.error("Unexpected error getting user display name for user: {}", authName, e);
            return authName;
        }
    }

    /**
     * Determines whether a menu button should be visible based on authentication and role requirements.
     * Always visible buttons shown regardless of authentication state. Logout button only for authenticated users.
     * Role-restricted buttons require both authentication and appropriate role assignment.
     *
     * @param menuButton the menu button to evaluate for visibility
     * @param auth the current authentication context
     * @param isAuthenticated whether the user is currently authenticated
     * @return true if the button should be visible, false otherwise
     */
    private boolean shouldShowButton(
            final MenuButton menuButton, final Authentication auth, final boolean isAuthenticated) {

        if (menuButton.isAlwaysVisible()) {
            return true;
        }

        if (isLogoutButton(menuButton)) {
            return isAuthenticated;
        }

        if (hasRoleRestrictions(menuButton)) {
            return isAuthenticated && hasRequiredRole(menuButton, auth);
        }

        return false;
    }

    /**
     * Checks if the menu button is the logout button.
     *
     * @param menuButton the menu button to check
     * @return true if it's the logout button
     */
    private boolean isLogoutButton(final MenuButton menuButton) {
        return LOGOUT_ROUTE.equals(menuButton.getRoute());
    }

    /**
     * Checks if the menu button has role restrictions.
     *
     * @param menuButton the menu button to check
     * @return true if it has role restrictions
     */
    private boolean hasRoleRestrictions(final MenuButton menuButton) {
        return menuButton.getRequiredRoles() != null
                && !menuButton.getRequiredRoles().isEmpty();
    }

    /**
     * Checks if the user has any of the required roles for the menu button.
     *
     * @param menuButton the menu button with role requirements
     * @param auth the current authentication context
     * @return true if user has required role
     */
    private boolean hasRequiredRole(final MenuButton menuButton, final Authentication auth) {
        if (auth == null) {
            return false;
        }

        Set<String> userAuthorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return menuButton.getRequiredRoles().stream().anyMatch(userAuthorities::contains);
    }

    /**
     * Creates a configured button for the specified menu button configuration.
     * Creates buttons with appropriate styling and click handlers based on the
     * menu button type. Special handling is provided for logout and settings buttons
     * to ensure proper functionality.
     *
     * @param menuButton the menu button configuration to create a button for
     * @return a configured Button component ready for use
     */
    private Button createButton(final MenuButton menuButton) {
        Button button = createButtonByRoute(menuButton);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, menuButton.getTestId());
        return button;
    }

    /**
     * Creates a button based on the menu button route.
     *
     * @param menuButton the menu button configuration
     * @return a configured button with appropriate click handler
     */
    private Button createButtonByRoute(final MenuButton menuButton) {
        String route = menuButton.getRoute();

        if (LOGOUT_ROUTE.equals(route)) {
            return createLogoutButton(menuButton);
        }

        if ((RouteConstants.ROOT_PATH + RouteConstants.SETTINGS_ROUTE).equals(route)) {
            return createSettingsButton(menuButton);
        }

        return createNavigationButton(menuButton);
    }

    /**
     * Creates a logout button with confirmation dialog.
     *
     * @param menuButton the menu button configuration
     * @return a configured logout button
     */
    private Button createLogoutButton(final MenuButton menuButton) {
        return ButtonHelper.createTertiaryButton(menuButton.getText(), e -> openLogoutDialog());
    }

    /**
     * Creates a settings button with practice settings dialog.
     *
     * @param menuButton the menu button configuration
     * @return a configured settings button
     */
    private Button createSettingsButton(final MenuButton menuButton) {
        return ButtonHelper.createTertiaryButton(menuButton.getText(), e -> {
            PracticeSettingsDialog dialog = new PracticeSettingsDialog(practiceSettingsService);
            getUI().ifPresent(ui -> ui.add(dialog));
            dialog.open();
        });
    }

    /**
     * Creates a navigation button for regular menu items.
     *
     * @param menuButton the menu button configuration
     * @return a configured navigation button
     */
    private Button createNavigationButton(final MenuButton menuButton) {
        return ButtonHelper.createTertiaryButton(
                menuButton.getText(), e -> NavigationHelper.navigateTo(menuButton.getRoute()));
    }

    /**
     * Opens a confirmation dialog for user logout.
     * Creates and displays a confirmation dialog to prevent accidental logouts.
     * Upon confirmation, performs the logout operation using Spring Security's
     * logout handler and redirects to the home page.
     */
    private void openLogoutDialog() {
        Dialog dialog = createLogoutDialog();
        getUI().ifPresent(ui -> ui.add(dialog));
        dialog.open();
    }

    /**
     * Creates the logout confirmation dialog with title, message, and action buttons.
     *
     * @return the configured logout dialog
     */
    private Dialog createLogoutDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName(CoreConstants.DIALOG_SM_CLASS);

        VerticalLayout layout = createDialogLayout(dialog);
        dialog.add(layout);

        configureDialogBehavior(dialog);
        return dialog;
    }

    /**
     * Creates the main layout for the logout dialog.
     *
     * @param dialog the dialog instance for cancel button reference
     * @return the configured vertical layout
     */
    private VerticalLayout createDialogLayout(final Dialog dialog) {
        VerticalLayout layout = new VerticalLayout();

        String confirmMessage = getTranslation(CoreConstants.AUTH_LOGOUT_CONFIRM_KEY);
        layout.add(new H3(confirmMessage));
        layout.add(new Span(confirmMessage));

        HorizontalLayout buttonsLayout = createButtonsLayout(dialog);
        layout.add(buttonsLayout);

        return layout;
    }

    /**
     * Creates the buttons layout for the logout dialog.
     *
     * @param dialog the dialog instance for cancel button reference
     * @return the configured horizontal layout with buttons
     */
    private HorizontalLayout createButtonsLayout(final Dialog dialog) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = createConfirmButton();
        Button cancelButton = createCancelButton(dialog);

        buttons.add(confirmButton, cancelButton);
        return buttons;
    }

    /**
     * Creates the confirm button for logout action.
     *
     * @return the configured confirm button
     */
    private Button createConfirmButton() {
        return ButtonHelper.createConfirmButton(getTranslation(CoreConstants.DIALOG_CONFIRM_KEY), e -> performLogout());
    }

    /**
     * Creates the cancel button for dialog closure.
     *
     * @param dialog the dialog instance to close
     * @return the configured cancel button
     */
    private Button createCancelButton(final Dialog dialog) {
        return ButtonHelper.createCancelButton(getTranslation(CoreConstants.DIALOG_CANCEL_KEY), e -> dialog.close());
    }

    /**
     * Performs the actual logout operation.
     */
    private void performLogout() {
        try {
            VaadinServletRequest request = VaadinServletRequest.getCurrent();
            if (request == null) {
                LOGGER.warn("Cannot perform logout: VaadinServletRequest is null");
                NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
                return;
            }

            new SecurityContextLogoutHandler().logout(request.getHttpServletRequest(), null, null);
            getUI().ifPresent(ui -> ui.getPage().setLocation(RouteConstants.ROOT_PATH));
        } catch (AuthenticationException e) {
            LOGGER.warn("Authentication error during logout: {}", e.getMessage());
            NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during logout", e);
            NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
        }
    }

    /**
     * Configures dialog behavior for closing.
     *
     * @param dialog the dialog to configure
     */
    private void configureDialogBehavior(final Dialog dialog) {
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
    }

    /**
     * Internal class representing a menu button configuration.
     * Encapsulates all the properties needed to create and configure a menu button,
     * including text, navigation route, test identifier, visibility rules, and role requirements.
     */
    private static class MenuButton {
        private final String text;
        private final String route;
        private final String testId;
        private final boolean alwaysVisible;
        private final List<String> requiredRoles;

        /**
         * Constructs a new MenuButton with basic configuration.
         *
         * @param textValue the display text for the button
         * @param routeValue the navigation route when the button is clicked
         * @param testIdValue the test identifier for automated testing
         * @param alwaysVisibleValue whether the button should always be visible
         */
        MenuButton(
                final String textValue,
                final String routeValue,
                final String testIdValue,
                final boolean alwaysVisibleValue) {
            this(textValue, routeValue, testIdValue, alwaysVisibleValue, new String[0]);
        }

        /**
         * Constructs a new MenuButton with role-based visibility control.
         *
         * @param textValue the display text for the button
         * @param routeValue the navigation route when the button is clicked
         * @param testIdValue the test identifier for automated testing
         * @param alwaysVisibleValue whether the button should always be visible
         * @param requiredRolesValue the roles required to see this button
         */
        MenuButton(
                final String textValue,
                final String routeValue,
                final String testIdValue,
                final boolean alwaysVisibleValue,
                final String... requiredRolesValue) {
            this.text = textValue;
            this.route = routeValue;
            this.testId = testIdValue;
            this.alwaysVisible = alwaysVisibleValue;
            this.requiredRoles = requiredRolesValue != null ? List.of(requiredRolesValue) : new ArrayList<>();
        }

        String getText() {
            return text;
        }

        String getRoute() {
            return route;
        }

        String getTestId() {
            return testId;
        }

        boolean isAlwaysVisible() {
            return alwaysVisible;
        }

        List<String> getRequiredRoles() {
            return requiredRoles;
        }
    }
}
