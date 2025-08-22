package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Utility class for centralized icon creation and management.
 *
 * <p>This utility class provides factory methods for creating consistently
 * styled icons throughout the application. It eliminates duplication
 * of icon creation patterns and ensures uniform appearance and behavior.</p>
 *
 * <p>The class offers:</p>
 * <ul>
 *   <li>Pre-configured Vaadin icons for common use cases</li>
 *   <li>Search, check, and close icon creation</li>
 *   <li>Consistent icon styling and sizing</li>
 *   <li>Centralized icon management for maintainability</li>
 * </ul>
 *
 * <p>All icons created through this utility automatically include
 * appropriate styling and sizing for consistent user experience
 * across the application.</p>
 */
public final class IconHelper {

    private IconHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a search icon for search functionality.
     *
     * <p>Creates a search icon component using Vaadin's built-in
     * search icon. This icon is commonly used for search buttons,
     * search fields, and search-related user interface elements.</p>
     *
     * @return a configured search icon component
     */
    public static Icon createSearchIcon() {
        return VaadinIcon.SEARCH.create();
    }

    /**
     * Creates a check icon for confirmation actions.
     *
     * <p>Creates a check mark icon component using Vaadin's built-in
     * check icon. This icon is commonly used for success indicators,
     * confirmation buttons, and completed task displays.</p>
     *
     * @return a configured check icon component
     */
    public static Icon createCheckIcon() {
        return VaadinIcon.CHECK.create();
    }

    /**
     * Creates a close icon for dismissal actions.
     *
     * <p>Creates a close/cross icon component using Vaadin's built-in
     * close icon. This icon is commonly used for close buttons,
     * dismiss dialogs, and cancel actions.</p>
     *
     * @return a configured close icon component
     */
    public static Icon createCloseIcon() {
        return VaadinIcon.CLOSE.create();
    }
}
