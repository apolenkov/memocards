package org.apolenkov.application.views.utils;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;

/**
 * Utility class for centralized text element creation and styling.
 *
 * <p>This utility class provides factory methods for creating consistently
 * styled text components throughout the application. It eliminates duplication
 * of text setup patterns and ensures uniform appearance and behavior.</p>
 *
 * <p>The class offers:</p>
 * <ul>
 *   <li>Main title creation with consistent styling</li>
 *   <li>Page title creation for content sections</li>
 *   <li>Section title creation with standardized appearance</li>
 *   <li>Consistent typography and spacing</li>
 * </ul>
 *
 * <p>All text components created through this utility automatically include
 * appropriate styling, sizing, and spacing for consistent user experience
 * across the application.</p>
 */
public final class TextHelper {

    private TextHelper() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a main title component with consistent styling.
     *
     * <p>Creates an H1 heading component that serves as the primary
     * title for pages and major sections. The title is styled with
     * consistent typography and spacing for visual hierarchy.</p>
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
     *
     * <p>Creates an H2 heading component that serves as a secondary
     * title for page sections and content areas. The title is styled
     * with consistent typography and appropriate sizing for content
     * hierarchy.</p>
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
     *
     * <p>Creates an H3 heading component that serves as a tertiary
     * title for subsections and content areas. The title is styled
     * with consistent typography and appropriate sizing for content
     * hierarchy.</p>
     *
     * @param text the text content for the section title
     * @return a configured H3 component with consistent styling
     */
    public static H3 createSectionTitle(String text) {
        H3 title = new H3(text);

        return title;
    }
}
