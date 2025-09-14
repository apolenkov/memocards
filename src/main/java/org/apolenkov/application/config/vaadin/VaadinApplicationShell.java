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
        settings.addFavIcon("icon", "icons/favicon.ico", "any");
        settings.addFavIcon("icon", "icons/favicon.svg", "image/svg+xml");
        settings.addFavIcon("apple-touch-icon", "icons/pixel-icon.svg", "180x180");

        // Add meta tags for better SEO and mobile experience
        settings.addMetaTag("description", "Memocards - Smart flashcards for effective learning");
        settings.addMetaTag("keywords", "flashcards, learning, education, memorization");
        settings.addMetaTag("author", "Memocards Team");
        settings.addMetaTag("viewport", "width=device-width, initial-scale=1.0");

        // PWA support
        settings.addMetaTag("theme-color", "#1976d2");
        settings.addMetaTag("apple-mobile-web-app-capable", "yes");
        settings.addMetaTag("apple-mobile-web-app-status-bar-style", "default");
    }
}
