package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.vaadin.VaadinApplicationShell;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.practice.components.PracticeSettingsComponents;
import org.apolenkov.application.views.practice.components.PracticeSettingsDialog;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Mobile navigation menu component using Vaadin SideNav for drawer.
 * Provides touch-optimized navigation interface with user authentication status and role-based menu items.
 *
 * <p>Features:
 * <ul>
 *   <li>User greeting section at the top</li>
 *   <li>SideNav with icon-based navigation items</li>
 *   <li>Role-based menu visibility (USER, ADMIN)</li>
 *   <li>Locale change support for dynamic translation updates</li>
 * </ul>
 */
@Component
@UIScope
public class MobileNavigationMenu extends VerticalLayout implements LocaleChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobileNavigationMenu.class);

    // ==================== Constants ====================

    private static final String ARIA_LABEL_ATTRIBUTE = "aria-label";
    private static final String ROLE_ATTRIBUTE = "role";
    private static final String BUTTON_ROLE = "button";
    private static final String TABINDEX_ATTRIBUTE = "tabindex";
    private static final String TABINDEX_VALUE = "0";

    // ==================== Dependencies ====================

    private final transient TopMenuAuthService authService;
    private final transient TopMenuLogoutDialog logoutDialog;
    private final transient org.apolenkov.application.service.settings.PracticeSettingsService practiceSettingsService;
    private final transient PracticeSettingsComponents settingsComponents;

    // Tracks if component has been initialized at least once
    private boolean hasBeenInitialized = false;

    // Tracks authentication state to prevent unnecessary refreshes
    private boolean wasAuthenticated = false;

    // ==================== Constructor ====================

    /**
     * Creates a new MobileNavigationMenu with required dependencies.
     *
     * @param authenticationService service for authentication operations
     * @param logoutDialogService service for logout dialog operations
     * @param settingsService service for practice settings
     * @param settingsComponentsService components for settings UI
     */
    public MobileNavigationMenu(
            final TopMenuAuthService authenticationService,
            final TopMenuLogoutDialog logoutDialogService,
            @Lazy final org.apolenkov.application.service.settings.PracticeSettingsService settingsService,
            @Lazy final PracticeSettingsComponents settingsComponentsService) {
        this.authService = authenticationService;
        this.logoutDialog = logoutDialogService;
        this.practiceSettingsService = settingsService;
        this.settingsComponents = settingsComponentsService;
    }

    // ==================== Lifecycle ====================

    /**
     * Initializes the menu components when attached to the UI.
     * At this point, all @UIScope components are ready and getTranslation() is safe to use.
     *
     * @param attachEvent the attaching event
     */
    @Override
    protected void onAttach(final AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        // Check if already initialized
        if (hasBeenInitialized) {
            return;
        }

        setSpacing(false);
        setPadding(false);
        setWidthFull();
        addClassName(CoreConstants.MOBILE_NAV_MENU_CLASS);

        refreshMenu();

        hasBeenInitialized = true;

        // Initialize authentication state tracking
        Authentication auth = authService.getCurrentAuthentication();
        wasAuthenticated = authService.isAuthenticated(auth);
    }

    /**
     * Refreshes the entire menu content based on current authentication state.
     * Creates fresh component instances to avoid state tree corruption.
     */
    public void refreshMenu() {
        removeAll();

        Authentication auth = authService.getCurrentAuthentication();
        boolean isAuthenticated = authService.isAuthenticated(auth);

        if (isAuthenticated) {
            createAuthenticatedMenu(auth);
        } else {
            createAnonymousMenu();
        }
    }

    /**
     * Refreshes menu only if authentication state has changed.
     * Prevents unnecessary re-rendering that causes flickering.
     */
    public void refreshMenuIfNeeded() {
        Authentication auth = authService.getCurrentAuthentication();
        boolean isAuthenticated = authService.isAuthenticated(auth);

        // Only refresh if authentication state changed
        if (isAuthenticated != wasAuthenticated) {
            refreshMenu();
            wasAuthenticated = isAuthenticated;
        }
    }

    // ==================== Menu Creation ====================

    /**
     * Creates menu for authenticated users.
     *
     * @param auth the authentication context
     */
    private void createAuthenticatedMenu(final Authentication auth) {
        // App title header
        Div header = createHeaderSection();
        add(header);

        // User greeting
        Div greetingSection = createGreetingSection(auth);
        add(greetingSection);

        // Navigation buttons (routes only) - no divider between greeting and menu
        VerticalLayout navigationButtons = new VerticalLayout();
        navigationButtons.addClassName(CoreConstants.MOBILE_NAV_SIDENAV_CLASS);
        navigationButtons.setPadding(true); // Ensure consistent spacing with action buttons
        navigationButtons.setSpacing(false);

        if (authService.hasUserRole(auth)) {
            navigationButtons.add(createDecksButton());
            navigationButtons.add(createStatsButton());
        }

        if (authService.hasAdminRole(auth)) {
            navigationButtons.add(createAdminContentNavItem());
        }

        add(navigationButtons);

        Hr divider2 = new Hr();
        divider2.addClassName(CoreConstants.MOBILE_NAV_DIVIDER_CLASS);
        add(divider2);

        // Action buttons container (non-navigation items)
        VerticalLayout actionsContainer = new VerticalLayout();
        actionsContainer.addClassName(CoreConstants.MOBILE_NAV_ACTIONS_CLASS);
        actionsContainer.setSpacing(false);
        actionsContainer.setPadding(false);
        actionsContainer.setMargin(false);

        if (authService.hasUserRole(auth)) {
            actionsContainer.add(createSettingsButton());
        }
        actionsContainer.add(createLogoutButton());

        add(actionsContainer);
    }

    /**
     * Creates menu for anonymous users (login/register).
     */
    private void createAnonymousMenu() {
        // App title header
        Div header = createHeaderSection();
        add(header);

        Hr divider1 = new Hr();
        divider1.addClassName(CoreConstants.MOBILE_NAV_DIVIDER_CLASS);
        add(divider1);

        // Actions container for buttons
        VerticalLayout actionsContainer = new VerticalLayout();
        actionsContainer.addClassName(CoreConstants.MOBILE_NAV_ACTIONS_CLASS);
        actionsContainer.setSpacing(false);
        actionsContainer.setPadding(false);
        actionsContainer.setMargin(false);

        // Login and Registration buttons for anonymous users
        actionsContainer.add(createLoginButton());
        actionsContainer.add(createRegistrationButton());

        add(actionsContainer);
    }

    // ==================== Section Creators ====================

    /**
     * Creates the header section with favicon, app title, and close button.
     *
     * @return configured header section
     */
    private Div createHeaderSection() {
        Div header = new Div();
        header.addClassName(CoreConstants.MOBILE_NAV_HEADER_CLASS);

        // Left: Favicon + Title
        Div leftSection = new Div();
        leftSection.addClassName(CoreConstants.MOBILE_NAV_HEADER_LEFT_CLASS);

        Image favicon = createFavicon();
        leftSection.add(favicon);

        H4 title = new H4(getTranslation(CoreConstants.APP_TITLE_KEY));
        title.addClassName(CoreConstants.MOBILE_NAV_TITLE_CLASS);
        leftSection.add(title);

        // Right: Close button
        Div rightSection = new Div();
        rightSection.addClassName(CoreConstants.MOBILE_NAV_HEADER_RIGHT_CLASS);

        Button closeButton = createCloseButton();
        rightSection.add(closeButton);

        header.add(leftSection, rightSection);
        return header;
    }

    /**
     * Creates the greeting section for authenticated users.
     *
     * @param auth the authentication context
     * @return configured greeting section
     */
    private Div createGreetingSection(final Authentication auth) {
        Div greeting = new Div();
        greeting.addClassName(CoreConstants.MOBILE_NAV_GREETING_CLASS);

        String displayName = authService.getUserDisplayName(auth);
        String greetingText = getTranslation(CoreConstants.MAIN_GREETING_KEY);

        Span greetingSpan = new Span(greetingText.replace(CoreConstants.PLACEHOLDER_0, displayName));
        greetingSpan.addClassName(CoreConstants.MOBILE_NAV_GREETING_TEXT_CLASS);

        greeting.add(greetingSpan);
        return greeting;
    }

    // ==================== SideNavItem Creators ====================

    /**
     * Creates decks navigation button.
     *
     * @return configured Button
     */
    private Button createDecksButton() {
        Button button =
                ButtonHelper.createButton(getTranslation(CoreConstants.MAIN_DECKS_KEY), VaadinIcon.RECORDS, e -> {
                    closeDrawer(); // Close drawer first for immediate response
                    UI.getCurrent().navigate(RouteConstants.ROOT_PATH + RouteConstants.DECKS_ROUTE);
                });

        button.setWidthFull();
        button.addClassName(CoreConstants.MOBILE_NAV_ACTION_BUTTON_CLASS);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_DECKS_TEST_ID);

        // Accessibility improvements
        button.getElement().setAttribute(ARIA_LABEL_ATTRIBUTE, getTranslation(CoreConstants.MAIN_DECKS_KEY));
        button.getElement().setAttribute(ROLE_ATTRIBUTE, BUTTON_ROLE);
        button.getElement().setAttribute(TABINDEX_ATTRIBUTE, TABINDEX_VALUE);

        return button;
    }

    /**
     * Creates stats navigation button.
     *
     * @return configured Button
     */
    private Button createStatsButton() {
        Button button = ButtonHelper.createButton(getTranslation(CoreConstants.MAIN_STATS_KEY), VaadinIcon.CHART, e -> {
            closeDrawer(); // Close drawer first for immediate response
            UI.getCurrent().navigate(RouteConstants.ROOT_PATH + RouteConstants.STATS_ROUTE);
        });

        button.setWidthFull();
        button.addClassName(CoreConstants.MOBILE_NAV_ACTION_BUTTON_CLASS);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_STATS_TEST_ID);

        // Accessibility improvements
        button.getElement().setAttribute(ARIA_LABEL_ATTRIBUTE, getTranslation(CoreConstants.MAIN_STATS_KEY));
        button.getElement().setAttribute(ROLE_ATTRIBUTE, BUTTON_ROLE);
        button.getElement().setAttribute(TABINDEX_ATTRIBUTE, TABINDEX_VALUE);

        return button;
    }

    /**
     * Creates admin content navigation item.
     *
     * @return configured SideNavItem
     */
    private SideNavItem createAdminContentNavItem() {
        SideNavItem item = new SideNavItem(
                getTranslation(CoreConstants.ADMIN_CONTENT_TITLE_KEY),
                RouteConstants.ROOT_PATH + RouteConstants.ADMIN_CONTENT_ROUTE);

        Icon icon = VaadinIcon.TOOLS.create();
        item.setPrefixComponent(icon);
        item.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_ADMIN_CONTENT_TEST_ID);

        // Close drawer immediately on click (same as Login/Register buttons)
        item.getElement().addEventListener("click", e -> closeDrawer());

        return item;
    }

    // ==================== Action Buttons (Non-Navigation) ====================

    /**
     * Creates settings button with dialog handler.
     * Uses Button instead of SideNavItem since it opens a dialog, not navigation.
     *
     * @return configured Button
     */
    private Button createSettingsButton() {
        Button button = ButtonHelper.createButton(
                getTranslation(CoreConstants.MAIN_SETTINGS_KEY),
                VaadinIcon.COG,
                e -> {
                    PracticeSettingsDialog dialog =
                            new PracticeSettingsDialog(practiceSettingsService, settingsComponents);
                    UI.getCurrent().add(dialog);
                    dialog.open();
                    closeDrawer();
                },
                ButtonVariant.LUMO_TERTIARY);

        button.setWidthFull();
        button.addClassName(CoreConstants.MOBILE_NAV_ACTION_BUTTON_CLASS);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_SETTINGS_TEST_ID);
        return button;
    }

    /**
     * Creates logout button with confirmation dialog.
     * Uses Button instead of SideNavItem since it opens a dialog, not navigation.
     *
     * @return configured Button
     */
    private Button createLogoutButton() {
        Button button = ButtonHelper.createButton(
                getTranslation(CoreConstants.MAIN_LOGOUT_KEY),
                VaadinIcon.SIGN_OUT,
                e -> {
                    logoutDialog.openLogoutDialog();
                    closeDrawer();
                },
                ButtonVariant.LUMO_TERTIARY);

        button.setWidthFull();
        button.addClassName(CoreConstants.MOBILE_NAV_ACTION_BUTTON_CLASS);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_LOGOUT_TEST_ID);
        return button;
    }

    // ==================== Helper Methods ====================

    /**
     * Closes the drawer using native Vaadin approach.
     * This method will be called from the parent AppLayout's afterNavigation().
     */
    private void closeDrawer() {
        getParent().ifPresent(parent -> {
            if (parent instanceof com.vaadin.flow.component.applayout.AppLayout appLayout) {
                appLayout.setDrawerOpened(false);
            }
        });
    }

    /**
     * Creates login button for anonymous users.
     *
     * @return configured login button
     */
    private Button createLoginButton() {
        Button button = ButtonHelper.createButton(
                getTranslation(CoreConstants.MAIN_LOGIN_KEY),
                VaadinIcon.SIGN_IN,
                e -> {
                    closeDrawer(); // Close drawer first for immediate response
                    UI.getCurrent().navigate(RouteConstants.ROOT_PATH + RouteConstants.LOGIN_ROUTE);
                },
                ButtonVariant.LUMO_PRIMARY);

        button.setWidthFull();
        button.addClassName(CoreConstants.MOBILE_NAV_ACTION_BUTTON_CLASS);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_LOGIN_TEST_ID);

        // Accessibility improvements
        button.getElement().setAttribute(ARIA_LABEL_ATTRIBUTE, getTranslation(CoreConstants.MAIN_LOGIN_KEY));
        button.getElement().setAttribute(ROLE_ATTRIBUTE, BUTTON_ROLE);
        button.getElement().setAttribute(TABINDEX_ATTRIBUTE, TABINDEX_VALUE);

        return button;
    }

    /**
     * Creates registration button for anonymous users.
     *
     * @return configured registration button
     */
    private Button createRegistrationButton() {
        Button button = ButtonHelper.createButton(
                getTranslation(CoreConstants.MAIN_REGISTER_KEY),
                VaadinIcon.USER_CARD,
                e -> {
                    closeDrawer(); // Close drawer first for immediate response
                    UI.getCurrent().navigate(RouteConstants.ROOT_PATH + RouteConstants.REGISTER_ROUTE);
                },
                ButtonVariant.LUMO_TERTIARY);

        button.setWidthFull();
        button.addClassName(CoreConstants.MOBILE_NAV_ACTION_BUTTON_CLASS);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_REGISTER_TEST_ID);

        // Accessibility improvements
        button.getElement().setAttribute(ARIA_LABEL_ATTRIBUTE, getTranslation(CoreConstants.MAIN_REGISTER_KEY));
        button.getElement().setAttribute(ROLE_ATTRIBUTE, BUTTON_ROLE);
        button.getElement().setAttribute(TABINDEX_ATTRIBUTE, TABINDEX_VALUE);

        return button;
    }

    /**
     * Creates favicon image for header.
     *
     * @return configured favicon image
     */
    private Image createFavicon() {
        Image favicon = new Image(
                new StreamResource(VaadinApplicationShell.ResourcePaths.FAVICON_SVG_NAME, () -> getClass()
                        .getResourceAsStream(VaadinApplicationShell.ResourcePaths.FAVICON_SVG_FULL_PATH)),
                getTranslation(CoreConstants.APP_TITLE_KEY));
        favicon.addClassName(CoreConstants.MOBILE_NAV_FAVICON_CLASS);
        return favicon;
    }

    /**
     * Creates close button for header.
     *
     * @return configured close button
     */
    private Button createCloseButton() {
        Button closeButton =
                ButtonHelper.createButton("", VaadinIcon.CLOSE, e -> closeDrawer(), ButtonVariant.LUMO_TERTIARY_INLINE);

        closeButton.addClassName(CoreConstants.MOBILE_NAV_CLOSE_BUTTON_CLASS);
        closeButton.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_CLOSE_TEST_ID);

        // Accessibility improvements
        closeButton
                .getElement()
                .setAttribute(ARIA_LABEL_ATTRIBUTE, getTranslation(CoreConstants.NAVIGATION_CLOSE_MENU_KEY));
        closeButton.getElement().setAttribute(ROLE_ATTRIBUTE, BUTTON_ROLE);
        closeButton.getElement().setAttribute(TABINDEX_ATTRIBUTE, TABINDEX_VALUE);

        return closeButton;
    }

    // ==================== Locale Change Observer ====================

    /**
     * Refreshes menu items when locale changes to update translations.
     * Only refreshes if component is already initialized (has children).
     *
     * @param event locale change event
     */
    @Override
    public void localeChange(final LocaleChangeEvent event) {
        LOGGER.debug("Locale changed in MobileNavigationMenu, refreshing menu");

        // Only refresh if already initialized
        if (hasBeenInitialized) {
            refreshMenu();
        } else {
            LOGGER.debug("MobileNavigationMenu not yet initialized, skipping locale change refresh");
        }
    }
}
