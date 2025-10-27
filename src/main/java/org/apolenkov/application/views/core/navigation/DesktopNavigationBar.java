package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.vaadin.VaadinApplicationShell;
import org.apolenkov.application.service.settings.PracticeSettingsService;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.practice.components.PracticeSettingsComponents;
import org.apolenkov.application.views.practice.components.PracticeSettingsDialog;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Desktop navigation bar component for wide screens (≥768px).
 * Provides horizontal navigation interface with logo, greeting, navigation buttons, and user menu.
 *
 * <p>This component is displayed in the navbar slot of AppLayout on desktop screens
 * and hidden on mobile devices where the drawer navigation is used instead.
 */
@Component
@UIScope
public class DesktopNavigationBar extends HorizontalLayout implements LocaleChangeObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopNavigationBar.class);

    // ==================== Dependencies ====================

    private final transient TopMenuAuthService authService;
    private final transient TopMenuLogoutDialog logoutDialog;
    private final transient PracticeSettingsService practiceSettingsService;
    private final transient PracticeSettingsComponents settingsComponents;

    // Tracks if component has been initialized at least once
    private boolean hasBeenInitialized = false;

    // Tracks authentication state to prevent unnecessary refreshes
    private boolean wasAuthenticated = false;

    // ==================== Constructor ====================

    /**
     * Creates a new DesktopNavigationBar with required dependencies.
     *
     * @param authenticationService service for authentication operations
     * @param logoutDialogService service for logout dialog operations
     * @param settingsService service for practice settings
     * @param settingsComponentsService components for settings UI
     */
    public DesktopNavigationBar(
            final TopMenuAuthService authenticationService,
            final TopMenuLogoutDialog logoutDialogService,
            @Lazy final PracticeSettingsService settingsService,
            @Lazy final PracticeSettingsComponents settingsComponentsService) {
        this.authService = authenticationService;
        this.logoutDialog = logoutDialogService;
        this.practiceSettingsService = settingsService;
        this.settingsComponents = settingsComponentsService;
    }

    // ==================== Lifecycle ====================

    /**
     * Initializes the navigation bar components when attached to the UI.
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

        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.BETWEEN);
        addClassName(CoreConstants.DESKTOP_NAV_BAR_CLASS);

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

        HorizontalLayout leftSection = createLeftSection(auth, isAuthenticated);
        HorizontalLayout buttonsSection = createMenuButtonsLayout(auth, isAuthenticated);

        add(leftSection, buttonsSection);
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

    // ==================== Layout Creation ====================

    /**
     * Creates the left section of the navigation bar containing logo and user greeting.
     *
     * @param auth the authentication context
     * @param isAuthenticated whether user is authenticated
     * @return the configured left section layout
     */
    private HorizontalLayout createLeftSection(final Authentication auth, final boolean isAuthenticated) {
        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(Alignment.CENTER);
        left.setSpacing(true);
        left.addClassName(CoreConstants.DESKTOP_NAV_LEFT_CLASS);

        Anchor title = createTitleAnchor();
        left.add(title);

        if (isAuthenticated) {
            String displayName = authService.getUserDisplayName(auth);
            String greetingText = getTranslation(CoreConstants.MAIN_GREETING_KEY);
            Div greeting = new Div();
            greeting.setText(greetingText.replace(CoreConstants.PLACEHOLDER_0, displayName));
            greeting.addClassName(CoreConstants.TOP_MENU_GREETING_CLASS);
            left.add(greeting);
        }

        return left;
    }

    /**
     * Creates title anchor with logo icon.
     *
     * @return new Anchor instance with logo
     */
    private Anchor createTitleAnchor() {
        Anchor anchor = new Anchor(RouteConstants.ROOT_PATH, "");

        Image navIcon = new Image();
        navIcon.setSrc(VaadinApplicationShell.ResourcePaths.LOGO_ICON);
        navIcon.setAlt(getTranslation(CoreConstants.APP_TITLE_KEY));
        anchor.add(navIcon);
        return anchor;
    }

    /**
     * Creates the horizontal layout containing all visible menu buttons.
     *
     * @param auth the authentication context
     * @param isAuthenticated whether user is authenticated
     * @return layout containing menu buttons
     */
    private HorizontalLayout createMenuButtonsLayout(final Authentication auth, final boolean isAuthenticated) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(Alignment.CENTER);
        buttonsLayout.addClassName(CoreConstants.DESKTOP_NAV_BUTTONS_CLASS);

        if (isAuthenticated) {
            if (authService.hasUserRole(auth)) {
                buttonsLayout.add(createDecksButton());
                buttonsLayout.add(createStatsButton());
                buttonsLayout.add(createSettingsButton());
            }

            if (authService.hasAdminRole(auth)) {
                buttonsLayout.add(createAdminContentButton());
            }

            buttonsLayout.add(createLogoutButton());
        }

        return buttonsLayout;
    }

    // ==================== Button Creation ====================

    /**
     * Creates a decks navigation button.
     *
     * @return configured button with navigation handler
     */
    private Button createDecksButton() {
        Button button = ButtonHelper.createTertiaryButton(
                getTranslation(CoreConstants.MAIN_DECKS_KEY),
                e -> NavigationHelper.navigateTo(RouteConstants.ROOT_PATH + RouteConstants.DECKS_ROUTE));
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_DECKS_TEST_ID);
        return button;
    }

    /**
     * Creates a stats navigation button.
     *
     * @return configured button with navigation handler
     */
    private Button createStatsButton() {
        Button button = ButtonHelper.createTertiaryButton(
                getTranslation(CoreConstants.MAIN_STATS_KEY),
                e -> NavigationHelper.navigateTo(RouteConstants.ROOT_PATH + RouteConstants.STATS_ROUTE));
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_STATS_TEST_ID);
        return button;
    }

    /**
     * Creates a settings button with practice settings dialog.
     *
     * @return configured button with dialog handler
     */
    private Button createSettingsButton() {
        Button button = ButtonHelper.createTertiaryButton(getTranslation(CoreConstants.MAIN_SETTINGS_KEY), e -> {
            PracticeSettingsDialog dialog = new PracticeSettingsDialog(practiceSettingsService, settingsComponents);
            UI.getCurrent().add(dialog);
            dialog.open();
        });
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_SETTINGS_TEST_ID);
        return button;
    }

    /**
     * Creates an admin content navigation button.
     *
     * @return configured button with navigation handler
     */
    private Button createAdminContentButton() {
        Button button = ButtonHelper.createTertiaryButton(
                getTranslation(CoreConstants.ADMIN_CONTENT_TITLE_KEY),
                e -> NavigationHelper.navigateTo(RouteConstants.ROOT_PATH + RouteConstants.ADMIN_CONTENT_ROUTE));
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_ADMIN_CONTENT_TEST_ID);
        return button;
    }

    /**
     * Creates a logout button with confirmation dialog.
     *
     * @return configured button with logout handler
     */
    private Button createLogoutButton() {
        Button button = ButtonHelper.createTertiaryButton(
                getTranslation(CoreConstants.MAIN_LOGOUT_KEY), e -> logoutDialog.openLogoutDialog());
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_LOGOUT_TEST_ID);
        return button;
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
        LOGGER.debug("Locale changed in DesktopNavigationBar, refreshing menu");

        // Only refresh if already initialized
        if (hasBeenInitialized) {
            refreshMenu();
        } else {
            LOGGER.debug("DesktopNavigationBar not yet initialized, skipping locale change refresh");
        }
    }
}
