package org.apolenkov.application.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.http.HttpServletResponse;
import org.apolenkov.application.views.LoginView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig extends VaadinWebSecurity {
    private final boolean prodProfileActive;
    private final DevAutoLoginFilter devAutoLoginFilter;

    @Autowired
    public SecurityConfig(Environment environment, @Autowired(required = false) DevAutoLoginFilter devAutoLoginFilter) {
        String[] profiles = environment != null ? environment.getActiveProfiles() : null;
        this.prodProfileActive =
                profiles != null && java.util.Arrays.asList(profiles).contains("prod");
        this.devAutoLoginFilter = devAutoLoginFilter;
    }

    // Backward-compatible constructor for tests/new instances that don't wire the optional filter
    public SecurityConfig(Environment environment) {
        String[] profiles = environment != null ? environment.getActiveProfiles() : null;
        this.prodProfileActive =
                profiles != null && java.util.Arrays.asList(profiles).contains("prod");
        this.devAutoLoginFilter = null;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Delegate matcher configuration to Vaadin's defaults to avoid conflicts
        super.configure(http);

        setLoginView(http, LoginView.class, "/");

        // CSRF via HttpOnly cookie; Vaadin handles token automatically. No need to expose to JS.
        CookieCsrfTokenRepository csrfRepo = new CookieCsrfTokenRepository();
        csrfRepo.setCookieCustomizer(cookie -> cookie.httpOnly(true));
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfRepo).ignoringRequestMatchers("/login"));

        // Unauthenticated → redirect to landing; AccessDenied → custom page for all profiles
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedHandler(accessDeniedHandler()));

        // Strict: only POST /logout with CSRF (configured above)
        http.logout(logout -> logout.logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true));

        // Dev-only: auto-login filter placed before UsernamePasswordAuthenticationFilter
        if (!prodProfileActive && devAutoLoginFilter != null) {
            http.addFilterBefore(devAutoLoginFilter, UsernamePasswordAuthenticationFilter.class);
        }

        if (prodProfileActive) {
            http.headers(headers -> headers.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; "
                    + "base-uri 'self'; form-action 'self'; object-src 'none'; frame-ancestors 'none'; "
                    + "img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
                    + "script-src 'self'; connect-src 'self'; font-src 'self' data:; worker-src 'self'")));
            // CSP tightened in prod (access denied page already configured above)
        } else {
            // Relaxed CSP for Vaadin dev mode (Vite, HMR, WebSockets, blob URLs)
            http.headers(
                    headers -> headers.contentSecurityPolicy(
                            csp -> csp.policyDirectives(
                                    "default-src 'self'; img-src 'self' data: blob:; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline' 'unsafe-eval' blob:; connect-src 'self' ws: wss:; font-src 'self' data:; frame-src 'self' blob:; worker-src 'self' blob:")));
        }
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {

            // Check if request is for UI (Vaadin) or API/static content
            String acceptHeader = request.getHeader("Accept");
            boolean isApiRequest = acceptHeader != null
                    && (acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)
                            || acceptHeader.contains(MediaType.APPLICATION_XML_VALUE))
                    && !acceptHeader.contains(MediaType.TEXT_HTML_VALUE);

            if (isApiRequest) {
                // Return JSON error for API requests
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Access denied\",\"status\":403}");
            } else {
                // Redirect to UI access denied page for browser requests
                response.sendRedirect("/access-denied");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Do not expose AuthenticationManager as a bean to avoid proxy recursion; obtain it from
    // AuthenticationConfiguration when needed
}
