package org.apolenkov.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initializes application components after Spring Boot startup.
 *
 * <p>This component uses Spring Boot's CommandLineRunner interface
 * to perform application initialization tasks after the application context
 * is fully loaded.
 *
 * <p>Use @Order annotation to control execution order if multiple
 * CommandLineRunner beans exist.
 */
@Component
@Order(1)
public class ApplicationInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInitializer.class);

    /**
     * Executes application initialization logic after Spring Boot startup.
     *
     * @param args command line arguments (unused in this implementation)
     */
    @Override
    public void run(final String... args) {
        LOGGER.info("Starting application initialization...");

        performInitialization();
        LOGGER.info("Application initialization completed successfully");
    }

    /**
     * Performs the actual initialization tasks.
     *
     * <p>This method can be extended to include:
     * - Database connection validation
     * - External service health checks
     * - Cache warming
     * - Scheduled task initialization
     * - Configuration validation
     */
    private void performInitialization() {
        LOGGER.debug("Performing application initialization tasks...");

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("All initialization tasks completed");
        }
    }
}
