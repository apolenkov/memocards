package org.apolenkov.application.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.AppShellSettings;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.Theme;
import org.springframework.context.annotation.Configuration;

/**
 * Vaadin application shell configuration.
 *
 * <p>This class configures the global application shell settings for the Vaadin
 * application, including theme selection, page title, and push communication
 * settings. It implements AppShellConfigurator to provide these configurations
 * at the application level.</p>
 *
 * <p>The configuration includes the custom "flashcards" theme, application title
 * "Memocards", and enables server push for real-time communication capabilities.</p>
 *
 */
@Configuration
@Theme(value = "flashcards")
@PageTitle("Memocards")
@Push
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        // Use the existing app icon as favicon (served from META-INF/resources/icons)
        settings.addFavIcon("icon", "icons/flashcards-logo.svg", "any");
        settings.addLink("apple-touch-icon", "icons/flashcards-logo.svg");
        settings.addMetaTag("theme-color", "var(--lumo-primary-color)");
    }
}
