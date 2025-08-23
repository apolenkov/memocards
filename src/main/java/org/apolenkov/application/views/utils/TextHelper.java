package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;

/**
 * Utility class for centralized text element creation and styling.
 * Provides factory methods for creating consistently styled text components.
 */
public final class TextHelper {

    private TextHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a main title component with consistent styling.
     * Creates an H1 heading component that serves as the primary title.
     *
     * @param text the text content for the main title
     * @return a configured H1 component with consistent styling
     */
    public static H1 createMainTitle(String text) {
        H1 title = new H1(text);

        return title;
    }

    /**
     * Creates a page title component with consistent styling.
     * Creates an H2 heading component that serves as a secondary title.
     *
     * @param text the text content for the page title
     * @return a configured H2 component with consistent styling
     */
    public static H2 createPageTitle(String text) {
        H2 title = new H2(text);

        return title;
    }

    /**
     * Creates a section title component with consistent styling.
     * Creates an H3 heading component that serves as a tertiary title.
     *
     * @param text the text content for the section title
     * @return a configured H3 component with consistent styling
     */
    public static H3 createSectionTitle(String text) {
        H3 title = new H3(text);

        return title;
    }
}
