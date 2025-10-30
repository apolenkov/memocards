package org.apolenkov.application.config.security;

/**
 * Constants for Spring Security role definitions.
 * Defines constant values for security roles used throughout the application.
 * Ensures consistency in role naming and prevents typos.
 */
public final class SecurityConstants {

    /**
     * Private constructor to prevent instantiation.
     */
    private SecurityConstants() {}

    /**
     * Standard user role for authenticated users.
     * Assigned to all authenticated users by default, provides access
     * to basic application functionality.
     */
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * Administrator role for privileged users.
     * Provides administrative privileges including user management,
     * system configuration, and access to administrative interfaces.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
