package org.apolenkov.application.views.shared.utils;

import com.vaadin.flow.router.BeforeEnterEvent;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for authentication-based redirect operations.
 * Provides helper methods for checking authentication status and redirecting users.
 */
public final class AuthRedirectHelper {

    private AuthRedirectHelper() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Redirects authenticated users to home page.
     * Used in anonymous views (login, register, forgot password) to prevent
     * authenticated users from accessing these pages.
     *
     * @param event the before enter event for redirection
     * @return true if redirect was performed, false otherwise
     */
    public static boolean redirectAuthenticatedToHome(final BeforeEnterEvent event) {
        if (isAuthenticated()) {
            NavigationHelper.forwardToHome(event);
            return true;
        }
        return false;
    }

    /**
     * Checks if current user is authenticated (not anonymous).
     *
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * Gets current authentication object.
     *
     * @return current authentication or null
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Gets authenticated username or null if not authenticated.
     *
     * @return username or null
     */
    public static String getAuthenticatedUsername() {
        Authentication auth = getCurrentAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return auth.getName();
        }
        return null;
    }
}
