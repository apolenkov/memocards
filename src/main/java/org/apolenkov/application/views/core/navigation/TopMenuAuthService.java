package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.security.SecurityConstants;
import org.apolenkov.application.domain.usecase.UserUseCase;
import org.apolenkov.application.views.shared.utils.NavigationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authentication and authorization operations in the top menu.
 * Handles user authentication status, role checks, and logout operations.
 */
@Service
public class TopMenuAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopMenuAuthService.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("org.apolenkov.application.audit");

    private final UserUseCase userUseCase;

    /**
     * Creates a new TopMenuAuthService with required dependencies.
     *
     * @param useCase service for user operations and current user information
     */
    public TopMenuAuthService(final UserUseCase useCase) {
        this.userUseCase = useCase;
    }

    /**
     * Gets the current authentication context.
     *
     * @return the current authentication or null
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Checks if the user is authenticated.
     *
     * @param auth the authentication context
     * @return true if user is authenticated
     */
    public boolean isAuthenticated(final Authentication auth) {
        return auth != null && !(auth instanceof AnonymousAuthenticationToken);
    }

    /**
     * Gets the user's display name, falling back to authentication name if needed.
     *
     * @param auth the authentication context
     * @return the user's display name
     */
    public String getUserDisplayName(final Authentication auth) {
        final String authName = auth.getName();

        try {
            final String displayName = userUseCase.getCurrentUser().getName();
            return (displayName == null || displayName.isBlank()) ? authName : displayName;
        } catch (AuthenticationException e) {
            LOGGER.warn("Failed to get user display name due to authentication issue: {}", e.getMessage());
            return authName;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to get user display name due to database issue: {}", e.getMessage());
            return authName;
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected error getting user display name for user: {}", authName, e);
            return authName;
        }
    }

    /**
     * Checks if user has USER role.
     *
     * @param auth the authentication context
     * @return true if user has USER role
     */
    public boolean hasUserRole(final Authentication auth) {
        return auth != null
                && auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(SecurityConstants.ROLE_USER::equals);
    }

    /**
     * Checks if user has ADMIN role.
     *
     * @param auth the authentication context
     * @return true if user has ADMIN role
     */
    public boolean hasAdminRole(final Authentication auth) {
        return auth != null
                && auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(SecurityConstants.ROLE_ADMIN::equals);
    }

    /**
     * Performs the actual logout operation.
     */
    public void performLogout() {
        final Authentication auth = getCurrentAuthentication();
        final String username = auth != null ? auth.getName() : "unknown";

        LOGGER.debug("Processing logout request for user: {}", username);

        try {
            final VaadinServletRequest request = VaadinServletRequest.getCurrent();
            final VaadinServletResponse response = VaadinServletResponse.getCurrent();
            if (request == null || response == null) {
                LOGGER.warn("Cannot perform logout: no current HTTP request or response");
                NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
                return;
            }

            new SecurityContextLogoutHandler()
                    .logout(request.getHttpServletRequest(), response.getHttpServletResponse(), auth);

            AUDIT_LOGGER.info("User logged out successfully: {}", username);
        } catch (AuthenticationException e) {
            AUDIT_LOGGER.warn("Logout failed: username={}, reason={}", username, e.getMessage());
            NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
        } catch (RuntimeException e) {
            AUDIT_LOGGER.error("Logout failed: username={}", username);
            LOGGER.error("Logout error", e);
            NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
        }
    }
}
