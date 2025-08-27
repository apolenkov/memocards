package org.apolenkov.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main entry point for the Flashcards Spring Boot application.
 *
 * <p>This class is responsible only for starting the Spring Boot application.
 * All other initialization logic is delegated to specialized components:
 * - {@link VaadinServiceInitializer} - Vaadin service configuration
 * - {@link ApplicationInitializer} - Application startup logic
 * - {@link LocaleConfiguration} - Locale management
 * - {@link ErrorHandlingConfiguration} - Error handling setup
 */
@SpringBootApplication
public final class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(final String[] args) {
        LOGGER.info("Starting Flashcards application...");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Application arguments: {}", java.util.Arrays.toString(args));
        }

        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);

        // Register shutdown hook for graceful application termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown hook triggered, cleaning up...");
            if (context.isActive()) {
                context.close();
            }
            LOGGER.info("Shutdown hook completed");
        }));

        LOGGER.info("Flashcards application started successfully");
    }
}
