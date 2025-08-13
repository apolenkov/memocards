package org.apolenkov.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application. Use the @PWA annotation make the application
 * installable on phones, tablets and some desktop browsers.
 */
@SpringBootApplication
@PageTitle("Flashcards")
@Theme(value = "flashcards")
public class Application implements AppShellConfigurator, VaadinServiceInitListener {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            VaadinSession session = uiEvent.getUI().getSession();
            Object preferred = session.getAttribute(
                    org.apolenkov.application.views.components.LanguageSwitcher.SESSION_LOCALE_KEY);
            if (preferred instanceof java.util.Locale locale) {
                uiEvent.getUI().setLocale(locale);
            } else {
                uiEvent.getUI().setLocale(java.util.Locale.ENGLISH);
            }
        });
    }
}
