package org.apolenkov.application.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter that enhances cookie security by adding SameSite and Secure attributes.
 * Intercepts cookie-setting operations and adds security attributes to user preference cookies.
 * Runs early in filter chain to ensure cookies are processed before being sent to client.
 */
@Component
@Order(100)
public class SameSiteCookieFilter implements Filter {

    /**
     * Enhances cookie security attributes for locale preferences.
     * Wraps HTTP response to intercept Set-Cookie headers and automatically adds
     * SameSite=Lax, Secure, and HttpOnly attributes for enhanced security.
     *
     * @param request the servlet request being processed
     * @param response the servlet response
     * @param chain the filter chain to continue processing
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) response;
        HttpServletResponseWrapper wrapped = new HttpServletResponseWrapper(httpResp) {
            @Override
            public void addHeader(String name, String value) {
                // Intercept Set-Cookie headers for locale preferences
                if ("Set-Cookie".equalsIgnoreCase(name) && value.startsWith(LocaleConstants.COOKIE_LOCALE_KEY + "=")) {
                    String v = value;
                    // Add SameSite=Lax if not present (prevents CSRF while maintaining functionality)
                    if (!v.toLowerCase().contains("samesite")) {
                        v = v + "; SameSite=Lax";
                    }
                    // Add Secure flag if not present (ensures HTTPS-only transmission)
                    if (!v.toLowerCase().contains("secure")) {
                        v = v + "; Secure";
                    }
                    // Add HttpOnly flag if not present (prevents JavaScript access)
                    if (!v.toLowerCase().contains("httponly")) {
                        v = v + "; HttpOnly";
                    }
                    super.addHeader(name, v);
                    return;
                }
                super.addHeader(name, value);
            }
        };
        chain.doFilter(request, wrapped);
    }
}
