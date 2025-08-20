package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;

/**
 * Utility class for centralized text element creation and styling.
 * Eliminates duplication of text element creation patterns across the application.
 */
public final class TextHelper {

    private TextHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Create a main title (H1)
     */
    public static H1 createMainTitle(String text) {
        H1 title = new H1(text);

        return title;
    }

    /**
     * Create a page title (H2)
     */
    public static H2 createPageTitle(String text) {
        H2 title = new H2(text);

        return title;
    }

    /**
     * Create a section title (H3)
     */
    public static H3 createSectionTitle(String text) {
        H3 title = new H3(text);

        return title;
    }
}
