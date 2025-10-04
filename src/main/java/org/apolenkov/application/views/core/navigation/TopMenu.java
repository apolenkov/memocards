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
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.config.vaadin.VaadinApplicationShell;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.practice.components.PracticeSettingsDialog;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    // Roles - using SecurityConstants

    // Routes - using RouteConstants

    // Styles
    private static final String DATA_TEST_ID_ATTRIBUTE = "data-test-id";

    // Logout button constants
    private static final String LOGOUT_ROUTE = RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE;
    private static final String LOGOUT_TEST_ID = "nav-logout";
    private static final boolean LOGOUT_ALWAYS_VISIBLE = false;

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

        title = new Anchor(RouteConstants.ROOT_PATH, "");

        Image navIcon = new Image(
                new StreamResource(VaadinApplicationShell.ResourcePaths.LOGO_ICON_NAME, () -> getClass()
                        .getResourceAsStream(VaadinApplicationShell.ResourcePaths.LOGO_ICON_FULL_PATH)),
                getTranslation("app.title"));
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
                getTranslation("main.decks"),
                RouteConstants.ROOT_PATH + RouteConstants.DECKS_ROUTE,
                "nav-decks",
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation("main.stats"),
                RouteConstants.ROOT_PATH + RouteConstants.STATS_ROUTE,
                "nav-stats",
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation("main.settings"),
                RouteConstants.ROOT_PATH + RouteConstants.SETTINGS_ROUTE,
                "nav-settings",
                false,
                SecurityConstants.ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation("admin.content.page.title"),
                RouteConstants.ROOT_PATH + RouteConstants.ADMIN_CONTENT_ROUTE,
                "nav-admin-content",
                false,
                SecurityConstants.ROLE_ADMIN));
        menuButtons.add(
                new MenuButton(getTranslation("main.logout"), LOGOUT_ROUTE, LOGOUT_TEST_ID, LOGOUT_ALWAYS_VISIBLE));
    }

    /**
     * Creates the horizontal layout containing all visible menu buttons.
     * Filters buttons based on user authentication and role requirements,
     * then creates and configures each button with appropriate styling and
     * click handlers.
     *
     * @return a horizontal layout containing the filtered menu buttons
     */
    private HorizontalLayout createMenuButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && !(auth instanceof AnonymousAuthenticationToken);

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

        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(Alignment.CENTER);
        left.setSpacing(true);
        left.add(title);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            String displayName;
            String authName = auth.getName();
            try {
                displayName = userUseCase.getCurrentUser().getName();
                if (displayName == null || displayName.isBlank()) {
                    displayName = authName;
                }
            } catch (Exception e) {
                displayName = authName;
            }
            Div greeting = new Div();
            greeting.setText(getTranslation("main.greeting", displayName));
            greeting.addClassName("top-menu__greeting");
            left.add(greeting);
        }

        add(left);
        add(createMenuButtonsLayout());
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

        if (menuButton.getRoute().equals(LOGOUT_ROUTE)) {
            return isAuthenticated;
        }

        if (menuButton.getRequiredRoles() != null
                && !menuButton.getRequiredRoles().isEmpty()) {
            if (!isAuthenticated) {
                return false;
            }

            for (String requiredRole : menuButton.getRequiredRoles()) {
                if (auth != null
                        && auth.getAuthorities().stream().anyMatch(a -> requiredRole.equals(a.getAuthority()))) {
                    return true;
                }
            }
            return false;
        }

        return false;
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
        Button button;

        switch (menuButton.getRoute()) {
            case LOGOUT_ROUTE -> {
                button = ButtonHelper.createTertiaryButton(menuButton.getText(), e -> openLogoutDialog());
                button.getElement().setAttribute(DATA_TEST_ID_ATTRIBUTE, menuButton.getTestId());
            }
            case RouteConstants.ROOT_PATH + RouteConstants.SETTINGS_ROUTE -> {
                button = ButtonHelper.createTertiaryButton(menuButton.getText(), e -> {
                    PracticeSettingsDialog dialog = new PracticeSettingsDialog(practiceSettingsService);
                    dialog.open();
                });
                button.getElement().setAttribute(DATA_TEST_ID_ATTRIBUTE, menuButton.getTestId());
            }
            default -> {
                button = ButtonHelper.createTertiaryButton(
                        menuButton.getText(), e -> NavigationHelper.navigateTo(menuButton.getRoute()));
                button.getElement().setAttribute(DATA_TEST_ID_ATTRIBUTE, menuButton.getTestId());
            }
        }

        return button;
    }

    /**
     * Opens a confirmation dialog for user logout.
     * Creates and displays a confirmation dialog to prevent accidental logouts.
     * Upon confirmation, performs the logout operation using Spring Security's
     * logout handler and redirects to the home page.
     */
    private void openLogoutDialog() {
        Dialog dialog = new Dialog();
        dialog.addClassName("dialog-sm");

        VerticalLayout layout = new VerticalLayout();
        layout.add(new H3(getTranslation("auth.logout.confirm")));
        layout.add(new Span(getTranslation("auth.logout.confirm")));

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttons.setWidthFull();

        Button confirmButton = ButtonHelper.createConfirmButton(getTranslation("dialog.confirm"), e -> {
            try {
                var req = VaadinServletRequest.getCurrent().getHttpServletRequest();
                new SecurityContextLogoutHandler().logout(req, null, null);
                getUI().ifPresent(ui -> ui.getPage()
                        .setLocation(RouteConstants.ROOT_PATH)); // Keep setLocation for logout (server redirect)
            } catch (Exception ignored) {
                NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
            }
            dialog.close();
        });

        Button cancelButton = ButtonHelper.createCancelButton(getTranslation("dialog.cancel"), e -> dialog.close());

        buttons.add(confirmButton, cancelButton);
        layout.add(buttons);
        dialog.add(layout);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);
        dialog.open();
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
