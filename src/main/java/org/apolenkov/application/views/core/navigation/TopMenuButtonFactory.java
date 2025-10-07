package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.service.PracticeSettingsService;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.apolenkov.application.views.practice.components.PracticeSettingsComponents;
import org.apolenkov.application.views.practice.components.PracticeSettingsDialog;
import org.apolenkov.application.views.shared.utils.ButtonHelper;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.springframework.stereotype.Component;

/**
 * Factory responsible for creating different types of buttons for the top menu.
 * Handles the creation of navigation buttons, logout buttons, and settings buttons
 * with appropriate click handlers and styling.
 * Uses @UIScope to access Vaadin I18N provider for translations.
 */
@Component
@UIScope
public class TopMenuButtonFactory implements LocaleChangeObserver {

    private final transient PracticeSettingsService practiceSettingsService;
    private final transient TopMenuLogoutDialog logoutDialog;
    private final transient PracticeSettingsComponents settingsComponents;

    /**
     * Creates a new TopMenuButtonFactory with required dependencies.
     *
     * @param settingsService service for practice session configuration
     * @param logoutDialogService service for logout dialog operations
     * @param settingsComponentsService components factory for creating settings UI elements
     */
    public TopMenuButtonFactory(
            final PracticeSettingsService settingsService,
            final TopMenuLogoutDialog logoutDialogService,
            final PracticeSettingsComponents settingsComponentsService) {
        this.practiceSettingsService = settingsService;
        this.logoutDialog = logoutDialogService;
        this.settingsComponents = settingsComponentsService;
    }

    /**
     * Creates a decks navigation button.
     *
     * @return configured button with navigation handler
     */
    public Button createDecksButton() {
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
    public Button createStatsButton() {
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
    public Button createSettingsButton() {
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
    public Button createAdminContentButton() {
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
    public Button createLogoutButton() {
        Button button = ButtonHelper.createTertiaryButton(
                getTranslation(CoreConstants.MAIN_LOGOUT_KEY), e -> logoutDialog.openLogoutDialog());
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, CoreConstants.NAV_LOGOUT_TEST_ID);
        return button;
    }

    /**
     * Gets translation for the specified key using current UI locale.
     *
     * @param key the translation key
     * @param params optional parameters
     * @return translated text
     */
    private String getTranslation(final String key, final Object... params) {
        return UI.getCurrent().getTranslation(key, params);
    }

    @Override
    public void localeChange(final LocaleChangeEvent event) {
        // Handle locale changes if needed in the future
    }
}
