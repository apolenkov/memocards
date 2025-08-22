package org.apolenkov.application.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.AppShellSettings;
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
        // Use provided static favicon.ico from META-INF/resources
        settings.addFavIcon("icon", "icons/favicon.ico", "any");
        settings.addMetaTag("theme-color", "var(--lumo-primary-color)");
    }
}
