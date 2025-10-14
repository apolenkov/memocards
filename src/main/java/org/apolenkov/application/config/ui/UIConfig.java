package org.apolenkov.application.config.ui;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration for UI-related settings.
 * Provides validated settings for UI components with compile-time safety.
 *
 * @param search search field configuration
 */
@ConfigurationProperties(prefix = "app.ui")
public record UIConfig(Search search) {

    /**
     * Search field configuration.
     * Controls search input behavior and debouncing settings.
     *
     * @param debounceMs debouncing timeout in milliseconds for search fields
     */
    public record Search(int debounceMs) {
        /**
         * Compact constructor with validation.
         */
        public Search {
            if (debounceMs < 0) {
                throw new IllegalArgumentException("Debounce timeout must be non-negative");
            }
            if (debounceMs > 5000) {
                throw new IllegalArgumentException("Debounce timeout must be <= 5000ms (too long for UX)");
            }
        }
    }

    /**
     * Compact constructor with validation.
     */
    public UIConfig {
        if (search == null) {
            throw new IllegalArgumentException("Search configuration cannot be null");
        }
    }
}
