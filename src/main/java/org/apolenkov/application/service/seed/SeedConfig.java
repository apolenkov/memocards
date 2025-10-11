package org.apolenkov.application.service.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for test data generation.
 * Groups all seed-related configuration values under app.seed.test prefix.
 *
 * @param test test data generation configuration
 */
@ConfigurationProperties(prefix = "app.seed")
public record SeedConfig(Test test) {

    /**
     * Test data generation configuration.
     *
     * @param enabled whether test data generation is enabled
     * @param testUserPassword password for generated test users
     * @param batch batch sizes for bulk operations
     * @param limits generation limits for different entity types
     */
    public record Test(boolean enabled, String testUserPassword, Batch batch, Limits limits) {}

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
