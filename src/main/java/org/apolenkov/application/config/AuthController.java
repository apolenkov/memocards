package org.apolenkov.application.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Placeholder to keep MVC package; logout is handled by Spring Security (POST /logout).
 */
@Controller
public class AuthController {

    @GetMapping("/logout")
    public String logoutRedirect() {
        // If someone GETs /logout, redirect to home. Real logout is POST /logout with CSRF.
        return "redirect:/home";
    }
}
