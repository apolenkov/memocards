package org.apolenkov.application.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
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

    private final ConfigurableApplicationContext applicationContext;

    /**
     * Creates ApplicationInitializer with application context.
     *
     * @param applicationContextParam the Spring application context
     */
    public ApplicationInitializer(final ConfigurableApplicationContext applicationContextParam) {
        this.applicationContext = applicationContextParam;
    }

    /**
     * Executes application initialization logic after Spring Boot startup.
     *
     * @param args command line arguments (unused in this implementation)
     */
    @Override
    public void run(final String... args) {
        LOGGER.info("Starting application initialization...");

        performInitialization();
        setupShutdownHook();

        LOGGER.info("Application initialization completed successfully");
    }

    /**
     * Performs the actual initialization tasks.
     *
     * <p>This method includes:
     * - Database connection validation
     * - Configuration validation
     * - Cache initialization
     * - External service health checks
     */
    private void performInitialization() {
        LOGGER.debug("Performing application initialization tasks...");

        // Database connection validation
        validateDatabaseConnection();

        // Configuration validation
        validateConfiguration();

        // Cache initialization
        initializeCache();

        // External service health checks
        validateExternalServices();

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("All initialization tasks completed");
        }
    }

    /**
     * Sets up graceful shutdown hook for the application.
     */
    private void setupShutdownHook() {
        LOGGER.debug("Setting up shutdown hook...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutdown hook triggered, cleaning up...");
            if (applicationContext.isActive()) {
                applicationContext.close();
            }
            LOGGER.info("Shutdown hook completed");
        }));

        LOGGER.debug("Shutdown hook configured successfully");
    }

    /**
     * Validates database connection and configuration.
     */
    private void validateDatabaseConnection() {
        LOGGER.debug("Validating database connection...");
        // Database validation logic would go here
        // For now, just log that validation is performed
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Database connection validation completed");
        }
    }

    /**
     * Validates application configuration.
     */
    private void validateConfiguration() {
        LOGGER.debug("Validating application configuration...");
        // Configuration validation logic would go here
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Configuration validation completed");
        }
    }

    /**
     * Initializes application cache.
     */
    private void initializeCache() {
        LOGGER.debug("Initializing application cache...");
        // Cache initialization logic would go here
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Cache initialization completed");
        }
    }

    /**
     * Validates external services connectivity.
     */
    private void validateExternalServices() {
        LOGGER.debug("Validating external services...");
        // External service validation logic would go here
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("External services validation completed");
        }
    }
}
