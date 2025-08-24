package org.apolenkov.application.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.usecase.UserUseCase;
import org.apolenkov.application.views.utils.ButtonHelper;
import org.apolenkov.application.views.utils.DialogHelper;
import org.apolenkov.application.views.utils.NavigationHelper;
import org.apolenkov.application.views.utils.Translator;
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

    // Roles
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Routes
    private static final String LOGOUT_ROUTE = "/logout";

    // Styles
    private static final String DATA_TESTID_ATTRIBUTE = "data-testid";

    private final List<MenuButton> menuButtons = new ArrayList<>();
    private final Anchor title;

    private final transient UserUseCase userUseCase;
    private final transient PracticeSettingsService practiceSettingsService;
    private final transient Translator translator;

    /**
     * Creates a new TopMenu with required dependencies.
     * Initializes the menu with application logo, title, and navigation buttons.
     *
     * @param userUseCase service for user operations and current user information
     * @param practiceSettingsService service for practice session configuration
     */
    public TopMenu(UserUseCase userUseCase, PracticeSettingsService practiceSettingsService, Translator translator) {
        this.userUseCase = userUseCase;
        this.practiceSettingsService = practiceSettingsService;
        this.translator = translator;
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);

        title = new Anchor("/", "");

        Image navIcon = new Image(
                new StreamResource(
                        "logo.svg", () -> getClass().getResourceAsStream("/META-INF/resources/icons/logo.svg")),
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
        menuButtons.add(new MenuButton(getTranslation("main.decks"), "/decks", "nav-decks", false, ROLE_USER));
        menuButtons.add(new MenuButton(getTranslation("main.stats"), "/stats", "nav-stats", false, ROLE_USER));
        menuButtons.add(new MenuButton(getTranslation("main.settings"), "/settings", "nav-settings", false, ROLE_USER));
        menuButtons.add(new MenuButton(
                getTranslation("admin.users.page.title"), "/admin/users", "nav-admin-users", false, ROLE_ADMIN));
        menuButtons.add(new MenuButton(
                getTranslation("admin.content.page.title"), "/admin/content", "nav-admin-content", false, ROLE_ADMIN));
        menuButtons.add(new MenuButton(
                getTranslation("admin.audit.page.title"), "/admin/audit", "nav-admin-audit", false, ROLE_ADMIN));
        menuButtons.add(new MenuButton(getTranslation("main.logout"), LOGOUT_ROUTE, "nav-logout", false));
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
    private boolean shouldShowButton(MenuButton menuButton, Authentication auth, boolean isAuthenticated) {
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
    private Button createButton(MenuButton menuButton) {
        Button button;

        if (menuButton.getRoute().equals(LOGOUT_ROUTE)) {
            button = ButtonHelper.createTertiaryButton(menuButton.getText(), e -> openLogoutDialog());
            button.getElement().setAttribute(DATA_TESTID_ATTRIBUTE, menuButton.getTestId());
        } else if (menuButton.getRoute().equals("/settings")) {
            button = ButtonHelper.createTertiaryButton(menuButton.getText(), e -> {
                PracticeSettingsDialog dialog = new PracticeSettingsDialog(practiceSettingsService);
                dialog.open();
            });
            button.getElement().setAttribute(DATA_TESTID_ATTRIBUTE, menuButton.getTestId());
        } else {
            button = ButtonHelper.createTertiaryButton(
                    menuButton.getText(), e -> NavigationHelper.navigateTo(menuButton.getRoute()));
            button.getElement().setAttribute(DATA_TESTID_ATTRIBUTE, menuButton.getTestId());
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
        Dialog dialog = DialogHelper.createConfirmationDialog(
                getTranslation("auth.logout.confirm"),
                getTranslation("auth.logout.confirm"),
                translator,
                () -> {
                    try {
                        var req = VaadinServletRequest.getCurrent().getHttpServletRequest();
                        new SecurityContextLogoutHandler().logout(req, null, null);
                        getUI().ifPresent(ui ->
                                ui.getPage().setLocation("/")); // Keep setLocation for logout (server redirect)
                    } catch (Exception ignored) {
                        getUI().ifPresent(ui ->
                                ui.navigate("error", com.vaadin.flow.router.QueryParameters.of("from", "home")));
                    }
                },
                null);
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
         * @param text the display text for the button
         * @param route the navigation route when the button is clicked
         * @param testId the test identifier for automated testing
         * @param alwaysVisible whether the button should always be visible
         */
        public MenuButton(String text, String route, String testId, boolean alwaysVisible) {
            this(text, route, testId, alwaysVisible, new String[0]);
        }

        /**
         * Constructs a new MenuButton with role-based visibility control.
         *
         * @param text the display text for the button
         * @param route the navigation route when the button is clicked
         * @param testId the test identifier for automated testing
         * @param alwaysVisible whether the button should always be visible
         * @param requiredRoles the roles required to see this button
         */
        public MenuButton(String text, String route, String testId, boolean alwaysVisible, String... requiredRoles) {
            this.text = text;
            this.route = route;
            this.testId = testId;
            this.alwaysVisible = alwaysVisible;
            this.requiredRoles = requiredRoles != null ? List.of(requiredRoles) : new ArrayList<>();
        }

        public String getText() {
            return text;
        }

        public String getRoute() {
            return route;
        }

        public String getTestId() {
            return testId;
        }

        public boolean isAlwaysVisible() {
            return alwaysVisible;
        }

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }
    }
}
