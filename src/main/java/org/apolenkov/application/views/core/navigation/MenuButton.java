package org.apolenkov.application.views.core.navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a menu button configuration for the top navigation menu.
 * Encapsulates all the properties needed to create and configure a menu button,
 * including text, navigation route, test identifier, visibility rules, and role requirements.
 */
public class MenuButton {
    private final String text;
    private final String route;
    private final String testId;
    private final boolean alwaysVisible;
    private final List<String> requiredRoles;

    /**
     * Constructs a new MenuButton with basic configuration.
     *
     * @param textValue the display text for the button
     * @param routeValue the navigation route when the button is clicked
     * @param testIdValue the test identifier for automated testing
     * @param alwaysVisibleValue whether the button should always be visible
     */
    public MenuButton(
            final String textValue,
            final String routeValue,
            final String testIdValue,
            final boolean alwaysVisibleValue) {
        this(textValue, routeValue, testIdValue, alwaysVisibleValue, new String[0]);
    }

    /**
     * Constructs a new MenuButton with role-based visibility control.
     *
     * @param textValue the display text for the button
     * @param routeValue the navigation route when the button is clicked
     * @param testIdValue the test identifier for automated testing
     * @param alwaysVisibleValue whether the button should always be visible
     * @param requiredRolesValue the roles required to see this button
     */
    public MenuButton(
            final String textValue,
            final String routeValue,
            final String testIdValue,
            final boolean alwaysVisibleValue,
            final String... requiredRolesValue) {
        this.text = textValue;
        this.route = routeValue;
        this.testId = testIdValue;
        this.alwaysVisible = alwaysVisibleValue;
        this.requiredRoles = requiredRolesValue != null ? List.of(requiredRolesValue) : new ArrayList<>();
    }

    /**
     * Gets the display text for the button.
     *
     * @return the button text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the navigation route for the button.
     *
     * @return the navigation route
     */
    public String getRoute() {
        return route;
    }

    /**
     * Gets the test identifier for the button.
     *
     * @return the test ID
     */
    public String getTestId() {
        return testId;
    }

    /**
     * Checks if the button should always be visible.
     *
     * @return true if always visible
     */
    public boolean isAlwaysVisible() {
        return alwaysVisible;
    }

    /**
     * Gets the required roles for the button.
     *
     * @return list of required roles
     */
    public List<String> getRequiredRoles() {
        return requiredRoles;
    }
}
