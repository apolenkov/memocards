package org.apolenkov.application.config;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Developer convenience filter: automatically authenticates a predefined user
 * when dev.auto-login.enabled=true (dev profile only).
 */
@Component
@Profile("!prod")
public class DevAutoLoginFilter extends OncePerRequestFilter {

    private static final Logger appLogger = LoggerFactory.getLogger(DevAutoLoginFilter.class);

    private final boolean autoLoginEnabled;
    private final String autoLoginUser;
    private final UserDetailsService userDetailsService;

    public DevAutoLoginFilter(
            @Value("${dev.auto-login.enabled:false}") boolean autoLoginEnabled,
            @Value("${dev.auto-login.user:user@example.com}") String autoLoginUser,
            UserDetailsService userDetailsService) {
        this.autoLoginEnabled = autoLoginEnabled;
        this.autoLoginUser = autoLoginUser;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip Vaadin internal (framework) requests to avoid interfering with
        // heartbeat, push, bootstrap, and uidl calls
        if (isVaadinInternalRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (autoLoginEnabled
                && SecurityContextHolder.getContext().getAuthentication() == null
                && isEligibleRequest(request)) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(autoLoginUser);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                appLogger.debug("Dev auto-login applied for user: {}", autoLoginUser);
            } catch (Exception e) {
                appLogger.warn("Dev auto-login failed for user: {}. Reason: {}", autoLoginUser, e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isEligibleRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip explicit auth endpoints; allow all others
        return !(path != null && (path.startsWith("/login") || path.startsWith("/logout")));
    }

    private boolean isVaadinInternalRequest(HttpServletRequest request) {
        String requestType = request.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        if (requestType == null) {
            return false;
        }
        for (HandlerHelper.RequestType type : HandlerHelper.RequestType.values()) {
            if (type.getIdentifier().equals(requestType)) {
                return true;
            }
        }
        return false;
    }
}
