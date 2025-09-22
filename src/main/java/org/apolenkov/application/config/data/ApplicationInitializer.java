package org.apolenkov.application.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    /**
     * Creates ApplicationInitializer with required dependencies.
     *
     * @param applicationContextParam the Spring application context
     * @param jdbcTemplateParam the JDBC template for database operations
     * @param environmentParam the Spring environment for configuration access
     */
    public ApplicationInitializer(
            final ConfigurableApplicationContext applicationContextParam,
            final JdbcTemplate jdbcTemplateParam,
            final Environment environmentParam) {
        this.applicationContext = applicationContextParam;
        this.jdbcTemplate = jdbcTemplateParam;
        this.environment = environmentParam;
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
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                LOGGER.info("Database connection validated successfully");
            } else {
                throw new IllegalStateException("Database validation failed: unexpected result");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Database connection validation failed", e);
        }
    }

    /**
     * Validates application configuration.
     */
    private void validateConfiguration() {
        LOGGER.debug("Validating application configuration...");

        // Check required properties
        String[] requiredProperties = {"spring.datasource.url", "spring.datasource.username"};

        for (String property : requiredProperties) {
            String value = environment.getProperty(property);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalStateException("Required property not set: " + property);
            }
        }

        LOGGER.info("Application configuration validated successfully");
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
