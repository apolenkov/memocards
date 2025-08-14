package org.apolenkov.application.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Adds SameSite and Secure attributes to non-functional cookies like preferredLocale.
 */
@Component
@Order(100)
public class SameSiteCookieFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResp = (HttpServletResponse) response;
        HttpServletResponseWrapper wrapped = new HttpServletResponseWrapper(httpResp) {
            @Override
            public void addHeader(String name, String value) {
                if ("Set-Cookie".equalsIgnoreCase(name) && value.startsWith(LocaleConstants.COOKIE_LOCALE_KEY + "=")) {
                    String v = value;
                    if (!v.toLowerCase().contains("samesite")) {
                        v = v + "; SameSite=Lax";
                    }
                    if (!v.toLowerCase().contains("secure")) {
                        v = v + "; Secure";
                    }
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
