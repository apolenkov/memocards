package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Utility class for centralized icon creation and management.
 * Provides factory methods for creating consistently styled icons.
 */
public final class IconHelper {

    private IconHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a search icon for search functionality.
     * Creates a search icon component using Vaadin's built-in search icon.
     *
     * @return a configured search icon component
     */
    public static Icon createSearchIcon() {
        return VaadinIcon.SEARCH.create();
    }

    /**
     * Creates a check icon for confirmation actions.
     * Creates a check mark icon component using Vaadin's built-in check icon.
     *
     * @return a configured check icon component
     */
    public static Icon createCheckIcon() {
        return VaadinIcon.CHECK.create();
    }

    /**
     * Creates a close icon for dismissal actions.
     * Creates a close/cross icon component using Vaadin's built-in close icon.
     *
     * @return a configured close icon component
     */
    public static Icon createCloseIcon() {
        return VaadinIcon.CLOSE.create();
    }
}
