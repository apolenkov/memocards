package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
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
     * Creates buttons directly based on user authentication and role requirements.
     *
     * @param auth the cached authentication context
     * @param isAuthenticated the cached authentication status
     * @param texts translated texts for menu buttons
     * @return a horizontal layout containing the filtered menu buttons
     */
    public HorizontalLayout createMenuButtonsLayout(
            final Authentication auth, final boolean isAuthenticated, final MenuButtonTexts texts) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        if (isAuthenticated) {
            // Add buttons if user has required roles
            if (authService.hasUserRole(auth)) {
                buttonsLayout.add(buttonFactory.createDecksButton(texts.decks()));
                buttonsLayout.add(buttonFactory.createStatsButton(texts.stats()));
                buttonsLayout.add(buttonFactory.createSettingsButton(texts.settings()));
            }

            // Admin button - only for admins
            if (authService.hasAdminRole(auth)) {
                buttonsLayout.add(buttonFactory.createAdminContentButton(texts.adminContent()));
            }

            // Logout - for all authenticated users
            buttonsLayout.add(buttonFactory.createLogoutButton(texts.logout()));
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
