package org.apolenkov.application.config.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration for seed data generation.
 * Provides validated settings for demo and test data with compile-time safety.
 *
 * @param demo demo user data configuration
 * @param test test data generation configuration
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedConfig(Demo demo, Test test) {

    /**
     * Demo user data configuration.
     * Contains credentials for development environment demo users (admin and regular user).
     *
     * @param enabled whether to seed demo users
     * @param adminPassword password for admin user
     * @param userPassword password for regular user
     */
    public record Demo(boolean enabled, String adminPassword, String userPassword) {
        /**
         * Compact constructor with validation.
         */
        public Demo {
            if (enabled) {
                if (adminPassword == null || adminPassword.isBlank()) {
                    throw new IllegalArgumentException("Demo admin password cannot be empty when demo seed is enabled");
                }
                if (userPassword == null || userPassword.isBlank()) {
                    throw new IllegalArgumentException("Demo user password cannot be empty when demo seed is enabled");
                }
            }
        }
    }

    /**
     * Test data generation configuration.
     * Controls bulk test data generation for development and performance testing.
     *
     * @param enabled whether to generate test data
     * @param testUserPassword default password for generated test users
     * @param batch batch sizes for bulk operations
     * @param limits generation limits for different entity types
     */
    public record Test(boolean enabled, String testUserPassword, Batch batch, Limits limits) {
        /**
         * Compact constructor with validation.
         */
        public Test {
            if (enabled) {
                if (testUserPassword == null || testUserPassword.isBlank()) {
                    throw new IllegalArgumentException("Test user password cannot be empty when test seed is enabled");
                }
                if (batch == null) {
                    throw new IllegalArgumentException("Batch configuration cannot be null when test seed is enabled");
                }
                if (limits == null) {
                    throw new IllegalArgumentException("Limits configuration cannot be null when test seed is enabled");
                }
            }
        }

        /**
         * Batch sizes for bulk operations.
         *
         * @param users batch size for user generation
         */
        public record Batch(int users) {
            /**
             * Compact constructor with validation.
             */
            public Batch {
                if (users <= 0) {
                    throw new IllegalArgumentException("Batch size must be positive");
                }
            }
        }

        /**
         * Generation limits for different entity types.
         *
         * @param users total users to generate
         * @param decksPerUser decks per user
         * @param cardsPerDeck cards per deck
         * @param news news articles count
         */
        public record Limits(int users, int decksPerUser, int cardsPerDeck, int news) {
            /**
             * Compact constructor with validation.
             */
            public Limits {
                if (users < 0 || decksPerUser < 0 || cardsPerDeck < 0 || news < 0) {
                    throw new IllegalArgumentException("Limits must be non-negative");
                }
            }
        }
    }

    /**
     * Compact constructor with validation.
     */
    public SeedConfig {
        if (demo == null) {
            throw new IllegalArgumentException("Demo configuration cannot be null");
        }
        if (test == null) {
            throw new IllegalArgumentException("Test configuration cannot be null");
        }
    }
}
