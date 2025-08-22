package org.apolenkov.application.service;

import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final org.apolenkov.application.service.user.JpaRegistrationService jpaRegistrationService;

    public AuthFacade(
            AuthenticationConfiguration authenticationConfiguration,
            org.apolenkov.application.service.user.JpaRegistrationService jpaRegistrationService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jpaRegistrationService = jpaRegistrationService;
    }

    public void registerUser(String username, String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < 8
                || !rawPassword.matches(".*\\d.*")
                || !rawPassword.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must be at least 8 characters and contain letters and digits");
        }

        jpaRegistrationService.register(username, username, rawPassword);
    }

    public void authenticateAndPersist(String username, String rawPassword) {
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(username, rawPassword);
        Authentication auth;
        try {
            auth = authenticationConfiguration.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            throw new InvalidPasswordException(e);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        VaadinServletRequest vsr = (VaadinServletRequest) VaadinService.getCurrentRequest();
        VaadinServletResponse vsp = (VaadinServletResponse) VaadinService.getCurrentResponse();
        if (vsr != null && vsp != null) {
            HttpServletRequest req = vsr.getHttpServletRequest();
            HttpServletResponse resp = vsp.getHttpServletResponse();
            new HttpSessionSecurityContextRepository().saveContext(context, req, resp);
        }
    }

    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(Exception error) {
            super(error);
        }
    }
}
