package org.apolenkov.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application. Use the @PWA annotation make the application
 * installable on phones, tablets and some desktop browsers.
 */
@SpringBootApplication
@PageTitle("app.title")
@Theme(value = "flashcards")
public class Application implements AppShellConfigurator, VaadinServiceInitListener {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            // Global UI-level error handler: navigate to /error on unhandled exceptions
            final var ui = uiEvent.getUI();
            ui.getSession().setErrorHandler(errorEvent -> {
                try {
                    // Get current route for 'from' parameter
                    String currentRoute =
                            ui.getInternals().getActiveViewLocation().getPath();
                    if (currentRoute != null && !currentRoute.isEmpty() && !currentRoute.equals("error")) {
                        ui.access(() ->
                                ui.navigate("error", com.vaadin.flow.router.QueryParameters.of("from", currentRoute)));
                    } else {
                        ui.access(() -> ui.navigate("error"));
                    }
                } catch (Exception ignored) {
                }
            });

            VaadinSession session = ui.getSession();
            // 1) Try from cookie first
            java.util.Locale cookieLocale = null;
            try {
                var req = (VaadinServletRequest) VaadinService.getCurrentRequest();
                if (req != null && req.getCookies() != null) {
                    for (jakarta.servlet.http.Cookie c : req.getCookies()) {
                        if (org.apolenkov.application.config.LocaleConstants.COOKIE_LOCALE_KEY.equals(c.getName())) {
                            cookieLocale = java.util.Locale.forLanguageTag(c.getValue());
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            if (cookieLocale != null) {
                session.setAttribute(org.apolenkov.application.config.LocaleConstants.SESSION_LOCALE_KEY, cookieLocale);
                ui.setLocale(cookieLocale);
                return;
            }

            // 2) Fallback to session attribute
            Object preferred =
                    session.getAttribute(org.apolenkov.application.config.LocaleConstants.SESSION_LOCALE_KEY);
            if (preferred instanceof java.util.Locale locale) {
                ui.setLocale(locale);
            } else {
                ui.setLocale(java.util.Locale.ENGLISH);
            }
        });
    }
}
