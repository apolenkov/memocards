package org.apolenkov.application.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import java.util.Arrays;
import org.apolenkov.application.views.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Delegate matcher configuration to Vaadin's defaults to avoid conflicts
        super.configure(http);
        setLoginView(http, LoginView.class, "/home");
        // Always go to /home after successful login (instead of returning to saved request like "/")
        http.formLogin(form -> form.defaultSuccessUrl("/home", true));

        // CSRF via cookie for use from Vaadin client; header name X-XSRF-TOKEN
        // Enforce CSRF for all state-changing endpoints including /logout
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        // Ensure XSRF cookie is present so our logout JS can read it
        http.addFilterAfter(new CsrfCookieFilter(), org.springframework.security.web.csrf.CsrfFilter.class);

        // Unauthenticated → redirect to landing; AccessDenied → custom page for all profiles
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                .accessDeniedPage("/access-denied"));

        // Strict: only POST /logout with CSRF
        http.csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
        );

        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProd) {
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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
