package org.apolenkov.application.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        // Invalidate HTTP session
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        // Remove remember-me cookie if present
        Cookie rm = new Cookie("remember-me", "");
        rm.setPath("/");
        rm.setMaxAge(0);
        rm.setHttpOnly(false);
        response.addCookie(rm);
        return "redirect:/home";
    }
}
