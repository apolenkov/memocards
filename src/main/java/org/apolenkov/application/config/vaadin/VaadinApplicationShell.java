package org.apolenkov.application.config.vaadin;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.context.annotation.Configuration;

/**
 * Global Vaadin application configuration.
 *
 * <p>This class configures the application shell with global settings including
 * theme selection, page metadata, push communication, and security headers.
 * It serves as the main entry point for Vaadin application configuration.
 */
@Configuration
@Theme(value = "flashcards")
@PageTitle("Memocards - Smart Flashcards Learning")
@Push
public class VaadinApplicationShell implements AppShellConfigurator {

    /**
     * Static resource paths for icons and assets.
     * These paths are used throughout the application for consistency.
     */
    public static final class ResourcePaths {
        private ResourcePaths() {
            // Utility class - prevent instantiation
        }

        // Base resource path
        public static final String META_INF_RESOURCES = "/META-INF/resources/";

        // Icon paths (relative to META_INF_RESOURCES)
        public static final String PIXEL_ICON = "icons/pixel-icon.svg";
        public static final String PIXEL_ICON_NAME = "pixel-icon.svg";
        public static final String FAVICON_ICO = "icons/favicon.ico";
        public static final String FAVICON_SVG = "icons/favicon.svg";
        public static final String LOGO_ICON = "icons/logo.svg";
        public static final String LOGO_ICON_NAME = "logo.svg";

        // Full paths (for resource streams)
        public static final String PIXEL_ICON_FULL_PATH = META_INF_RESOURCES + PIXEL_ICON;
        public static final String LOGO_ICON_FULL_PATH = META_INF_RESOURCES + LOGO_ICON;
    }

    /**
     * Configures global page settings including favicon, meta tags, and security headers.
     *
     * <p>This method is called during application startup to configure
     * the HTML page that wraps all Vaadin views.
     *
     * @param settings the page settings to configure (non-null)
     */
    @Override
    public void configurePage(final AppShellSettings settings) {
        // Configure favicon for different devices and sizes
        settings.addFavIcon("icon", ResourcePaths.FAVICON_ICO, "any");
        settings.addFavIcon("icon", ResourcePaths.FAVICON_SVG, "image/svg+xml");
        settings.addFavIcon("apple-touch-icon", ResourcePaths.PIXEL_ICON, "180x180");

        // Add meta tags for better SEO and mobile experience
        settings.addMetaTag("description", "Memocards - Smart flashcards for effective learning");
        settings.addMetaTag("keywords", "flashcards, learning, education, memorization");
        settings.addMetaTag("author", "Memocards Team");
        settings.addMetaTag("viewport", "width=device-width, initial-scale=1.0");
    }
}
