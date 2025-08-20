package org.apolenkov.application.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures the XSRF cookie is generated on GET requests by touching the CsrfToken.
 */
class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Object attr = request.getAttribute(CsrfToken.class.getName());
        if (attr instanceof CsrfToken token) {
            // Force generation of the cookie via CsrfFilter
            token.getToken();
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
