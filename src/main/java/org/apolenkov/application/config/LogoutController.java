package org.apolenkov.application.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class LogoutController {

    @GetMapping(path = "/logout-confirm")
    @ResponseBody
    public ResponseEntity<String> confirm(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        String param = token != null ? token.getParameterName() : "_csrf";
        String val = token != null ? token.getToken() : "";
        String html = "<!doctype html><html><head><meta charset='utf-8'><title>Logout</title></head><body>"
                + "<script>"
                + "if(!confirm('Вы действительно хотите выйти?')){window.location='/home';}else{"
                + "var f=document.createElement('form');f.method='POST';f.action='/logout';"
                + "var i=document.createElement('input');i.type='hidden';i.name='" + param + "';i.value='" + val
                + "';f.appendChild(i);"
                + "document.body.appendChild(f);f.submit();}"
                + "</script>"
                + "</body></html>";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + "; charset=UTF-8")
                .body(html);
    }
}
