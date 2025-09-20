package org.apolenkov.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Configuration class for TestContainers setup.
 */
public class TestContainersConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersConfig.class);

    /**
     * Custom PostgreSQL container implementation for testing.
     */
    public static final class CustomPostgreSQLContainer extends PostgreSQLContainer<CustomPostgreSQLContainer> {
        private static final String IMAGE_VERSION = "postgres:15";
        private static CustomPostgreSQLContainer container;

        /**
         * Creates a new PostgreSQL container instance.
         */
        public CustomPostgreSQLContainer() {
            super(IMAGE_VERSION);
        }

        /**
         * Gets or creates a singleton instance of the container.
         *
         * @return the container instance
         */
        public static CustomPostgreSQLContainer getInstance() {
            if (container == null) {
                container = new CustomPostgreSQLContainer();
                container.withDatabaseName("testdb");
                container.withUsername("testuser");
                container.withPassword("testpass");
            }
            return container;
        }

        /**
         * Starts the PostgreSQL container and logs the JDBC URL.
         */
        @Override
        public void start() {
            super.start();
            LOGGER.info("PostgreSQL container started: url={}", getJdbcUrl());
        }

        /**
         * Stops the PostgreSQL container.
         */
        @Override
        public void stop() {
            super.stop();
        }
    }
}
