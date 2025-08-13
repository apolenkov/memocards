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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure public endpoints BEFORE Vaadin default anyRequest rules
        http.authorizeHttpRequests(
                registry -> registry.requestMatchers("/", "/login", "/register", "/images/**", "/icons/**")
                        .permitAll());

        super.configure(http);
        setLoginView(http, LoginView.class, "/home");
        // Always go to /home after successful login (instead of returning to saved request like "/")
        http.formLogin(form -> form.defaultSuccessUrl("/home", true));

        // Remember-me so you don't have to login every time in dev
        http.rememberMe(remember -> remember.key("flashcards-remember-me-key")
                .tokenValiditySeconds(60 * 60 * 24 * 30) // 30 days
                .alwaysRemember(true));

        // Unauthenticated access → redirect to landing instead of /login
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")));

        // Logout handled via MVC controller (/logout GET)
        http.logout(logout -> logout.logoutUrl("/perform-logout").logoutSuccessUrl("/home"));

        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (isProd) {
            http.headers(
                    headers -> headers.contentSecurityPolicy(
                            csp -> csp.policyDirectives(
                                    "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; script-src 'self'; connect-src 'self'; font-src 'self' data:; frame-src 'self'; worker-src 'self'")));
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
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        manager.createUser(User.withUsername("user")
                .password(encoder.encode("password"))
                .roles("USER")
                .build());
        manager.createUser(User.withUsername("admin")
                .password(encoder.encode("admin"))
                .roles("USER", "ADMIN")
                .build());
        return manager;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
