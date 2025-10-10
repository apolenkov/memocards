package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.server.VaadinServletRequest;
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
import org.springframework.stereotype.Component;

/**
 * Service responsible for authentication and authorization operations in the top menu.
 * Handles user authentication status, role checks, and logout operations.
 */
@Component
public class TopMenuAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopMenuAuthService.class);

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
        String authName = auth.getName();

        try {
            String displayName = userUseCase.getCurrentUser().getName();
            return (displayName == null || displayName.isBlank()) ? authName : displayName;
        } catch (AuthenticationException e) {
            LOGGER.warn("Failed to get user display name due to authentication issue: {}", e.getMessage());
            return authName;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to get user display name due to database issue: {}", e.getMessage());
            return authName;
        } catch (Exception e) {
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
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
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
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(SecurityConstants.ROLE_ADMIN::equals);
    }

    /**
     * Performs the actual logout operation.
     */
    public void performLogout() {
        try {
            VaadinServletRequest request = VaadinServletRequest.getCurrent();
            if (request == null) {
                LOGGER.warn("Cannot perform logout: VaadinServletRequest is null");
                NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
                return;
            }

            new SecurityContextLogoutHandler().logout(request.getHttpServletRequest(), null, null);
        } catch (AuthenticationException e) {
            LOGGER.warn("Authentication error during logout: {}", e.getMessage());
            NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during logout", e);
            NavigationHelper.navigateToError(RouteConstants.HOME_ROUTE);
        }
    }
}
