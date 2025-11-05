package org.apolenkov.application.domain.constants;

/**
 * Business logic constants for the application.
 * Centralizes numeric constants used in business logic and validation.
 */
public final class BusinessConstants {

    // Percentage Constants
    /**
     * Maximum percentage value (100%).
     * Used for progress percentage validation and clamping.
     */
    public static final int PERCENTAGE_MAX = 100;

    // Time Constants
    /**
     * Number of milliseconds in a second.
     * Used for time conversions and calculations.
     */
    public static final long SECOND_MILLIS = 1000L;

    /**
     * Number of seconds in a minute.
     * Used for duration formatting and validation.
     */
    public static final long MINUTE_SECONDS = 60L;

    /**
     * Number of seconds in an hour.
     * Used for duration formatting and validation.
     */
    public static final long HOUR_SECONDS = 3600L;

    // Monitoring Constants
    /**
     * Connection pool utilization warning threshold (90%).
     * Used to warn when connection pool is near exhaustion.
     */
    public static final int UTILIZATION_WARN_THRESHOLD = 90;

    // UI Configuration Constants
    /**
     * Maximum debounce timeout in milliseconds (5000ms).
     * Used for search field debounce validation.
     */
    public static final int DEBOUNCE_MAX_MS = 5000;

    /**
     * Maximum page size (500 items).
     * Used for pagination validation to prevent performance degradation.
     */
    public static final int PAGE_SIZE_MAX = 500;

    // Validation Constants
    /**
     * Minimum name length (2 characters).
     * Used for user name validation.
     */
    public static final int NAME_MIN_LENGTH = 2;

    /**
     * Minimum password length (8 characters).
     * Used for password validation following security requirements.
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    // Security Constants
    /**
     * Password reset token expiration time in hours (24 hours).
     * Balances security with usability, following industry standard.
     */
    public static final int TOKEN_EXPIRATION_HOURS = 24;

    // Logging Constants
    /**
     * Maximum text length for log truncation (50 characters).
     * Used to truncate long text in log messages for readability.
     */
    public static final int MAX_LOG_TEXT_LENGTH = 50;

    // Cache Constants
    /**
     * Cache invalidation cooldown in milliseconds (300ms).
     * Used for debouncing cache invalidations during rapid user interactions.
     * Balances UX responsiveness with performance.
     */
    public static final long CACHE_INVALIDATION_COOLDOWN_MS = 300L;

    /**
     * Low cache hit rate threshold (50%).
     * Used for monitoring cache effectiveness and warning when hit rate is below threshold.
     */
    public static final double CACHE_LOW_HIT_RATE_THRESHOLD = 0.5;

    // Batch Operations Constants
    /**
     * Default batch size for database operations (1000 items).
     * Used for batch insert/update operations to optimize performance.
     */
    public static final int BATCH_SIZE_DEFAULT = 1000;

    // Pagination Constants
    /**
     * Single page threshold (1 page).
     * Used to determine if pagination is needed.
     */
    public static final int SINGLE_PAGE_THRESHOLD = 1;

    // Private constructor to prevent instantiation
    private BusinessConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
