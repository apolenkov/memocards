package org.apolenkov.application.config;

/**
 * Constants for Spring Security role definitions.
 *
 * <p>This utility class defines constant values for the security roles used
 * throughout the application. These constants ensure consistency in role
 * naming and prevent typos when referencing security roles in annotations
 * and configuration.</p>
 *
 * <p>The class is designed as a utility class and cannot be instantiated.
 * All role constants follow Spring Security's naming convention with the
 * "ROLE_" prefix.</p>
 *
 */
public final class SecurityConstants {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class is designed as a utility class containing only constants
     * and should not be instantiated.</p>
     */
    private SecurityConstants() {}

    /**
     * Standard user role for authenticated users.
     *
     * <p>This role is assigned to all authenticated users by default and
     * provides access to basic application functionality.</p>
     */
    public static final String ROLE_USER = "ROLE_USER";

    /**
     * Administrator role for privileged users.
     *
     * <p>This role provides administrative privileges including user management,
     * system configuration, and access to administrative interfaces.</p>
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
