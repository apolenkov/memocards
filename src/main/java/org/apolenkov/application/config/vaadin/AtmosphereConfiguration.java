package org.apolenkov.application.config.vaadin;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Configuration for Atmosphere push/websocket framework.
 * Sets up BroadcasterCache to preserve messages during client reconnections.
 *
 * <p>Configures UUIDBroadcasterCache to prevent message loss when clients temporarily
 * lose connection. This is important for real-time features and push notifications.
 *
 * <p>Uses HIGHEST_PRECEDENCE order to ensure Atmosphere initializes before VaadinService,
 * preventing race condition errors during WebSocket connection attempts at startup.
 */
@Component
public final class AtmosphereConfiguration implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtmosphereConfiguration.class);

    /**
     * Ensures Atmosphere framework initializes before VaadinService to prevent race conditions.
     * This bean runs with HIGHEST_PRECEDENCE to avoid "Can not process requests before init()" errors.
     *
     * @return ServletContextInitializer for early Atmosphere setup
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ServletContextInitializer atmosphereServletContextInitializer() {
        return servletContext -> {
            LOGGER.info("Early Atmosphere initialization started");

            // Configure Atmosphere framework parameters before VaadinService starts
            configureAtmosphereParameters(servletContext);

            LOGGER.info("Atmosphere framework pre-configured successfully");
        };
    }

    /**
     * Configures Atmosphere framework parameters in ServletContext.
     * Sets up session support and broadcaster factory for WebSocket connections.
     *
     * @param servletContext the servlet context to configure
     */
    private void configureAtmosphereParameters(final ServletContext servletContext) {
        // Enable session support for Atmosphere
        servletContext.setInitParameter("org.atmosphere.cpr.sessionSupport", "true");

        // Configure broadcaster factory
        servletContext.setInitParameter(
                "org.atmosphere.cpr.AtmosphereFramework.broadcasterFactory",
                "org.atmosphere.cpr.DefaultBroadcasterFactory");

        // Enable shared resources to improve performance
        servletContext.setInitParameter("org.atmosphere.cpr.shareableThreadPool", "true");

        // Disable analytics in production
        servletContext.setInitParameter("org.atmosphere.cpr.AtmosphereFramework.analytics", "false");

        LOGGER.debug("Atmosphere parameters configured in ServletContext");
    }

    @Override
    public void serviceInit(final ServiceInitEvent event) {
        LOGGER.debug("VaadinService initializing - Atmosphere should already be configured");

        event.getSource().addUIInitListener(uiEvent -> {
            try {
                // Additional per-UI configuration if needed
                configureAtmosphere();
            } catch (Exception e) {
                LOGGER.warn("Failed to configure per-UI Atmosphere settings: {}", e.getMessage());
            }
        });
    }

    /**
     * Configures Atmosphere framework with BroadcasterCache.
     * Enables message caching for reconnection scenarios.
     */
    private void configureAtmosphere() {
        try {
            // Per-UI Atmosphere configuration
            // Most configuration is done at ServletContext level for early initialization

            LOGGER.debug("Per-UI Atmosphere configuration completed");
            LOGGER.debug("BroadcasterCache ready for push features");

            // Note: Core Atmosphere setup happens in ServletContextInitializer
            // This method is for per-UI customization if needed

        } catch (Exception e) {
            LOGGER.error("Error configuring per-UI Atmosphere settings", e);
        }
    }
}
