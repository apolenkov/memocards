package org.apolenkov.application.views.core.navigation;

import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.Set;
import java.util.stream.Collectors;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.usecase.UserUseCase;
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
@UIScope
public class TopMenuAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopMenuAuthService.class);

    // Logout button constants
    private static final String LOGOUT_ROUTE = RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE;

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
     * Determines whether a menu button should be visible based on authentication and role requirements.
     * Always visible buttons shown regardless of authentication state. Logout button only for authenticated users.
     * Role-restricted buttons require both authentication and appropriate role assignment.
     *
     * @param menuButton the menu button to evaluate for visibility
     * @param auth the current authentication context
     * @param isAuthenticated whether the user is currently authenticated
     * @return true if the button should be visible, false otherwise
     */
    public boolean shouldShowButton(
            final MenuButton menuButton, final Authentication auth, final boolean isAuthenticated) {

        // Guard clause: always visible buttons
        if (menuButton.isAlwaysVisible()) {
            return true;
        }

        // Guard clause: logout button for authenticated users only
        if (isLogoutButton(menuButton)) {
            return isAuthenticated;
        }

        // Guard clause: role-restricted buttons require authentication and proper role
        if (hasRoleRestrictions(menuButton)) {
            return isAuthenticated && hasRequiredRole(menuButton, auth);
        }

        // Default: button not visible
        return false;
    }

    /**
     * Checks if the menu button is the logout button.
     *
     * @param menuButton the menu button to check
     * @return true if it's the logout button
     */
    public boolean isLogoutButton(final MenuButton menuButton) {
        return LOGOUT_ROUTE.equals(menuButton.getRoute());
    }

    /**
     * Checks if the menu button has role restrictions.
     *
     * @param menuButton the menu button to check
     * @return true if it has role restrictions
     */
    public boolean hasRoleRestrictions(final MenuButton menuButton) {
        return menuButton.getRequiredRoles() != null
                && !menuButton.getRequiredRoles().isEmpty();
    }

    /**
     * Checks if the user has any of the required roles for the menu button.
     *
     * @param menuButton the menu button with role requirements
     * @param auth the current authentication context
     * @return true if user has required role
     */
    public boolean hasRequiredRole(final MenuButton menuButton, final Authentication auth) {
        if (auth == null) {
            return false;
        }

        Set<String> userAuthorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return menuButton.getRequiredRoles().stream().anyMatch(userAuthorities::contains);
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
