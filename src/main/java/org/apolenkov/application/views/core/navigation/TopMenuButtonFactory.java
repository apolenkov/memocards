package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.practice.components.PracticeSettingsDialog;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.springframework.stereotype.Component;

/**
 * Factory responsible for creating different types of buttons for the top menu.
 * Handles the creation of navigation buttons, logout buttons, and settings buttons
 * with appropriate click handlers and styling.
 */
@Component
@UIScope
public class TopMenuButtonFactory {

    private final PracticeSettingsService practiceSettingsService;
    private final TopMenuLogoutDialog logoutDialog;

    /**
     * Creates a new TopMenuButtonFactory with required dependencies.
     *
     * @param settingsService service for practice session configuration
     * @param logoutDialogService service for logout dialog operations
     */
    public TopMenuButtonFactory(
            final PracticeSettingsService settingsService, final TopMenuLogoutDialog logoutDialogService) {
        this.practiceSettingsService = settingsService;
        this.logoutDialog = logoutDialogService;
    }

    /**
     * Creates a decks navigation button.
     *
     * @param text the button text
     * @return configured button with navigation handler
     */
    public Button createDecksButton(final String text) {
        Button button = ButtonHelper.createTertiaryButton(
                text, e -> NavigationHelper.navigateTo(RouteConstants.ROOT_PATH + RouteConstants.DECKS_ROUTE));
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_DECKS_TEST_ID);
        return button;
    }

    /**
     * Creates a stats navigation button.
     *
     * @param text the button text
     * @return configured button with navigation handler
     */
    public Button createStatsButton(final String text) {
        Button button = ButtonHelper.createTertiaryButton(
                text, e -> NavigationHelper.navigateTo(RouteConstants.ROOT_PATH + RouteConstants.STATS_ROUTE));
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_STATS_TEST_ID);
        return button;
    }

    /**
     * Creates a settings button with practice settings dialog.
     *
     * @param text the button text
     * @return configured button with dialog handler
     */
    public Button createSettingsButton(final String text) {
        Button button = ButtonHelper.createTertiaryButton(text, e -> {
            PracticeSettingsDialog dialog = new PracticeSettingsDialog(practiceSettingsService);
            UI.getCurrent().add(dialog);
            dialog.open();
        });
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_SETTINGS_TEST_ID);
        return button;
    }

    /**
     * Creates an admin content navigation button.
     *
     * @param text the button text
     * @return configured button with navigation handler
     */
    public Button createAdminContentButton(final String text) {
        Button button = ButtonHelper.createTertiaryButton(
                text, e -> NavigationHelper.navigateTo(RouteConstants.ROOT_PATH + RouteConstants.ADMIN_CONTENT_ROUTE));
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_ADMIN_CONTENT_TEST_ID);
        return button;
    }

    /**
     * Creates a logout button with confirmation dialog.
     *
     * @param text the button text
     * @return configured button with logout handler
     */
    public Button createLogoutButton(final String text) {
        Button button = ButtonHelper.createTertiaryButton(text, e -> logoutDialog.openLogoutDialog());
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_LOGOUT_TEST_ID);
        return button;
    }
}
