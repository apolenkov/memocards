package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apolenkov.application.views.core.constants.CoreConstants;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Service responsible for creating and managing UI layouts for the top menu.
 * Handles the creation of menu sections, user greetings, and menu button layouts.
 */
@Component
public class TopMenuLayoutService {

    private final TopMenuAuthService authService;
    private final TopMenuButtonFactory buttonFactory;

    /**
     * Creates a new TopMenuLayoutService with required dependencies.
     * Uses @Lazy for buttonFactory to avoid early initialization of @UIScope bean.
     *
     * @param authenticationService service for authentication operations
     * @param buttonFactoryService factory for creating menu buttons (lazy-initialized)
     */
    public TopMenuLayoutService(
            final TopMenuAuthService authenticationService, @Lazy final TopMenuButtonFactory buttonFactoryService) {
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
            String displayName = authService.getUserDisplayName(auth);
            Div greeting = new Div();
            greeting.setText(greetingText.replace(CoreConstants.PLACEHOLDER_0, displayName));
            greeting.addClassName(CoreConstants.TOP_MENU_GREETING_CLASS);
            left.add(greeting);
        }

        return left;
    }

    /**
     * Creates the horizontal layout containing all visible menu buttons.
     * Creates buttons directly based on user authentication and role requirements.
     * Translations are handled internally by button factory.
     *
     * @param auth the cached authentication context
     * @param isAuthenticated the cached authentication status
     * @return a horizontal layout containing the filtered menu buttons
     */
    public HorizontalLayout createMenuButtonsLayout(final Authentication auth, final boolean isAuthenticated) {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        if (isAuthenticated) {
            addUserButtons(auth, buttonsLayout);
            addAdminButton(auth, buttonsLayout);
            addLogoutButton(buttonsLayout);
        }

        return buttonsLayout;
    }

    /**
     * Adds user role buttons if user has USER role.
     *
     * @param auth the authentication context
     * @param layout the layout to add buttons to
     */
    private void addUserButtons(final Authentication auth, final HorizontalLayout layout) {
        if (authService.hasUserRole(auth)) {
            layout.add(buttonFactory.createDecksButton());
            layout.add(buttonFactory.createStatsButton());
            layout.add(buttonFactory.createSettingsButton());
        }
    }

    /**
     * Adds admin button if user has ADMIN role.
     *
     * @param auth the authentication context
     * @param layout the layout to add button to
     */
    private void addAdminButton(final Authentication auth, final HorizontalLayout layout) {
        if (authService.hasAdminRole(auth)) {
            layout.add(buttonFactory.createAdminContentButton());
        }
    }

    /**
     * Adds logout button for all authenticated users.
     *
     * @param layout the layout to add button to
     */
    private void addLogoutButton(final HorizontalLayout layout) {
        layout.add(buttonFactory.createLogoutButton());
    }
}
