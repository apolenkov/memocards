package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.function.UnaryOperator;
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

    // Logout button constants
    private static final String LOGOUT_ROUTE = RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE;

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
     * Creates a configured button for the specified menu button configuration.
     * Creates buttons with appropriate styling and click handlers based on the
     * menu button type. Special handling is provided for logout and settings buttons
     * to ensure proper functionality.
     *
     * @param menuButton the menu button configuration to create a button for
     * @param translationProvider function to get translations for button text
     * @return a configured Button component ready for use
     */
    public Button createButton(final MenuButton menuButton, final UnaryOperator<String> translationProvider) {
        Button button = createButtonByRoute(menuButton, translationProvider);
        button.getElement().setAttribute(CoreConstants.DATA_TEST_ID_ATTRIBUTE, menuButton.getTestId());
        return button;
    }

    /**
     * Creates a button based on the menu button route.
     *
     * @param menuButton the menu button configuration
     * @param translationProvider function to get translations for button text
     * @return a configured button with appropriate click handler
     */
    private Button createButtonByRoute(final MenuButton menuButton, final UnaryOperator<String> translationProvider) {
        String route = menuButton.getRoute();

        if (LOGOUT_ROUTE.equals(route)) {
            return createLogoutButton(menuButton, translationProvider);
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
     * @param translationProvider function to get translations for button text
     * @return a configured logout button
     */
    private Button createLogoutButton(final MenuButton menuButton, final UnaryOperator<String> translationProvider) {
        return ButtonHelper.createTertiaryButton(
                menuButton.getText(), e -> logoutDialog.openLogoutDialog(translationProvider));
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
            // UI context will be handled by the dialog itself
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
}
