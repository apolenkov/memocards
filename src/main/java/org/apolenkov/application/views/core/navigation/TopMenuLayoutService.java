package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.ArrayList;
import java.util.List;
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
     * @param decksText the translated text for decks button
     * @param statsText the translated text for stats button
     * @param settingsText the translated text for settings button
     * @param adminContentText the translated text for admin content button
     * @param logoutText the translated text for logout button
     */
    public void initializeMenuButtons(
            final String decksText,
            final String statsText,
            final String settingsText,
            final String adminContentText,
            final String logoutText) {
        menuButtons.clear();
        menuButtons.add(MenuButtonFactory.createDecksButton(decksText));
        menuButtons.add(MenuButtonFactory.createStatsButton(statsText));
        menuButtons.add(MenuButtonFactory.createSettingsButton(settingsText));
        menuButtons.add(MenuButtonFactory.createAdminContentButton(adminContentText));
        menuButtons.add(MenuButtonFactory.createLogoutButton(logoutText));
    }

    /**
     * Creates the left section of the menu containing title and user greeting.
     *
     * @param title the title anchor component
     * @param auth the cached authentication context
     * @param isAuthenticated the cached authentication status
     * @param greetingText the translated greeting text
     * @return the configured left section layout
     */
    public HorizontalLayout createLeftSection(
            final Anchor title, final Authentication auth, final boolean isAuthenticated, final String greetingText) {
        HorizontalLayout left = new HorizontalLayout();
        left.setAlignItems(FlexComponent.Alignment.CENTER);
        left.setSpacing(true);
        left.add(title);

        if (isAuthenticated) {
            Div greeting = createUserGreeting(auth, greetingText);
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
     * @return a horizontal layout containing the filtered menu buttons
     */
    public HorizontalLayout createMenuButtonsLayout(final Authentication auth, final boolean isAuthenticated) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        for (MenuButton menuButton : menuButtons) {
            if (authService.shouldShowButton(menuButton, auth, isAuthenticated)) {
                Button button = buttonFactory.createButton(menuButton);
                buttonsLayout.add(button);
            }
        }

        return buttonsLayout;
    }

    /**
     * Creates the user greeting component.
     *
     * @param auth the authentication context
     * @param greetingText the translated greeting text
     * @return the configured greeting div
     */
    private Div createUserGreeting(final Authentication auth, final String greetingText) {
        String displayName = authService.getUserDisplayName(auth);

        Div greeting = new Div();
        greeting.setText(greetingText.replace("{0}", displayName));
        greeting.addClassName(CoreConstants.TOP_MENU_GREETING_CLASS);

        return greeting;
    }
}
