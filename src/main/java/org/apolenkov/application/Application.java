package org.apolenkov.application;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the Cards Spring Boot application.
 *
 * <p>This class is responsible for starting the Spring Boot application.
 * All initialization logic is delegated to ApplicationInitializer.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public final class Application {

    /**
     * Private constructor to prevent instantiation.
     * This class is designed to be used as a Spring Boot application entry point.
     */
    private Application() {
        // Utility class - should not be instantiated
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    /**
     * Starts the Spring Boot application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(final String[] args) {
        LOGGER.info("Starting Cards application...");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Application arguments: {}", Arrays.toString(args));
        }

        SpringApplication.run(Application.class, args);

        LOGGER.info("Cards application started successfully");
    }
}
