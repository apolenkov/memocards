package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Utility class for centralized icon creation and management.
 * Eliminates duplication of icon creation patterns across the application.
 */
public final class IconHelper {

    private IconHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a search icon
     */
    public static Icon createSearchIcon() {
        return VaadinIcon.SEARCH.create();
    }

    /**
     * Create a check icon
     */
    public static Icon createCheckIcon() {
        return VaadinIcon.CHECK.create();
    }

    /**
     * Create a close icon
     */
    public static Icon createCloseIcon() {
        return VaadinIcon.CLOSE.create();
    }
}
