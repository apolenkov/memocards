package org.apolenkov.application.config.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.apolenkov.application.config.constants.RouteConstants;
import org.apolenkov.application.config.logging.MdcFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the application.
 * Uses VaadinWebSecurity with modern security practices for Vaadin 24.x.
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig extends VaadinWebSecurity {

    private final MdcFilter mdcFilter;

    /**
     * Creates security configuration.
     *
     * @param mdcFilterParam the MDC filter for request logging
     */
    public SecurityConfig(final MdcFilter mdcFilterParam) {
        this.mdcFilter = mdcFilterParam;
    }

    /**
     * Uses VaadinWebSecurity with enhanced CSRF protection and security headers.
     *
     * @param http the HttpSecurity builder
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        super.configure(http);

        setLoginView(http, RouteConstants.ROOT_PATH + RouteConstants.LOGIN_ROUTE);

        // Authentication and access control
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(
                new LoginUrlAuthenticationEntryPoint(RouteConstants.ROOT_PATH + RouteConstants.LOGIN_ROUTE)));

        // Logout configuration with session cleanup
        http.logout(logout -> logout.logoutUrl(RouteConstants.ROOT_PATH + RouteConstants.LOGOUT_ROUTE)
                .logoutSuccessUrl(RouteConstants.ROOT_PATH + RouteConstants.HOME_ROUTE)
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true));

        // Security headers for modern security practices - minimal set
        http.headers(
                headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny) // Clickjacking protection
                        .contentTypeOptions(contentTypeOptions -> {}) // MIME sniffing protection
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig // Enforce HTTPS
                                .maxAgeInSeconds(31536000)));

        // Add MDC filter for request correlation logging
        http.addFilterBefore(mdcFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Creates password encoder for secure password hashing.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
