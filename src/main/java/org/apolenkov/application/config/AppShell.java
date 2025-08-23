package org.apolenkov.application.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.Theme;
import org.springframework.context.annotation.Configuration;

/**
 * Vaadin application shell configuration.
 * Configures global application shell settings including theme selection,
 * page title, and push communication. Uses custom "flashcards" theme and
 * enables server push for real-time communication.
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
