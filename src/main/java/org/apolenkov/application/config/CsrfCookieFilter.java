package org.apolenkov.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures the XSRF cookie is generated on GET requests by touching the CsrfToken.
 * This filter is essential for Vaadin applications to ensure CSRF tokens are always available.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CsrfCookieFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only process GET requests to avoid interfering with POST requests
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            Object attr = request.getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
            if (attr instanceof org.springframework.security.web.csrf.CsrfToken token) {
                String tokenValue = token.getToken();
                LOGGER.debug(
                        "CSRF token generated for request: {} -> {}",
                        request.getRequestURI(),
                        tokenValue != null ? "SUCCESS" : "FAILED");
            } else {
                LOGGER.debug("No CSRF token found in request attributes for: {}", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        // Skip Vaadin internal requests and common static resources
        return uri.startsWith("/VAADIN/")
                || uri.startsWith("/HEARTBEAT/")
                || uri.startsWith("/UIDL/")
                || uri.startsWith("/css/")
                || uri.startsWith("/images/")
                || uri.startsWith("/icons/")
                || uri.startsWith("/js/")
                || uri.startsWith("/webjars/")
                || uri.startsWith("/frontend/")
                || uri.equals("/favicon.ico");
    }
}
