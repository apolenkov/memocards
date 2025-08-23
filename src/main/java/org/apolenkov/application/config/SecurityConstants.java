package org.apolenkov.application.config;

/**
 * Constants for Spring Security role definitions.
 *
 * <p>Defines constant values for security roles used throughout the application.
 * Ensures consistency in role naming and prevents typos. All role constants
 * follow Spring Security's naming convention with "ROLE_" prefix.</p>
 */
public final class SecurityConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private SecurityConstants() {}

    /**
     * Standard user role for authenticated users.
     *
     * <p>Assigned to all authenticated users by default, provides access
     * to basic application functionality.</p>
     */
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * Administrator role for privileged users.
     *
     * <p>Provides administrative privileges including user management,
     * system configuration, and access to administrative interfaces.</p>
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
