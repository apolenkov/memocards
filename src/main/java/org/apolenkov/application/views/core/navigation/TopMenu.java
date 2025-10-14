package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.UIScope;
import jakarta.annotation.PostConstruct;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.vaadin.VaadinApplicationShell;
import org.apolenkov.application.service.settings.PracticeSettingsService;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.practice.components.PracticeSettingsComponents;
import org.apolenkov.application.views.practice.components.PracticeSettingsDialog;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Top navigation menu component for the application.
 * Provides navigation interface with user authentication status and role-based menu items.
 *
 * <p>Simplified version consolidating button creation and layout logic for better code locality.
 */
@Component
@UIScope
public class TopMenu extends HorizontalLayout implements LocaleChangeObserver {

    private final transient TopMenuAuthService authService;
    private final transient TopMenuLogoutDialog logoutDialog;
    private final transient PracticeSettingsService practiceSettingsService;
    private final transient PracticeSettingsComponents settingsComponents;

    /**
     * Creates a new TopMenu with required dependencies.
     *
     * @param authenticationService service for authentication operations
     * @param logoutDialogService service for logout dialog operations
     * @param settingsService service for practice settings
     * @param settingsComponentsService components for settings UI
     */
    public TopMenu(
            final TopMenuAuthService authenticationService,
            final TopMenuLogoutDialog logoutDialogService,
            @Lazy final PracticeSettingsService settingsService,
            @Lazy final PracticeSettingsComponents settingsComponentsService) {
        this.authService = authenticationService;
        this.logoutDialog = logoutDialogService;
        this.practiceSettingsService = settingsService;
        this.settingsComponents = settingsComponentsService;
    }

    /**
     * Initializes the menu components after dependency injection is complete.
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

    // ==================== Layout Creation ====================

    /**
     * Creates the left section of the menu containing title and user greeting.
     *
     * @param auth the authentication context
     * @param isAuthenticated whether user is authenticated
     * @return the configured left section layout
     */
    private HorizontalLayout createLeftSection(final Authentication auth, final boolean isAuthenticated) {
        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(Alignment.CENTER);
        left.setSpacing(true);

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

        Image navIcon = new Image(
                new StreamResource(VaadinApplicationShell.ResourcePaths.LOGO_ICON_NAME, () -> getClass()
                        .getResourceAsStream(VaadinApplicationShell.ResourcePaths.LOGO_ICON_FULL_PATH)),
                getTranslation(CoreConstants.APP_TITLE_KEY));
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

    /**
     * Refreshes menu items when locale changes to update translations.
     *
     * @param event locale change event
     */
    @Override
    public void localeChange(final LocaleChangeEvent event) {
        // Refresh menu on locale change to update button labels
        refreshMenu();
    }
}
