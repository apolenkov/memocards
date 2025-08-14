package org.apolenkov.application.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.apolenkov.application.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@org.junit.jupiter.api.Tag("integration")
class SecurityIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void anonymous_can_access_public_pages() {
        ResponseEntity<String> r1 = rest.getForEntity("/", String.class);
        ResponseEntity<String> r2 = rest.getForEntity("/login", String.class);
        ResponseEntity<String> r3 = rest.getForEntity("/register", String.class);
        org.assertj.core.api.Assertions.assertThat(r1.getStatusCode().is2xxSuccessful())
                .isTrue();
        org.assertj.core.api.Assertions.assertThat(r2.getStatusCode().is2xxSuccessful())
                .isTrue();
        org.assertj.core.api.Assertions.assertThat(r3.getStatusCode().is2xxSuccessful())
                .isTrue();
    }

    @Test
    void anonymous_redirected_on_protected_route() {
        ResponseEntity<String> r = rest.getForEntity("/home", String.class);
        boolean redirected = r.getStatusCode().is3xxRedirection();
        boolean loginContent = (r.getBody() != null)
                && (r.getBody().toLowerCase().contains("login")
                        || r.getBody().toLowerCase().contains("vaadin-login"));
        org.assertj.core.api.Assertions.assertThat(redirected || loginContent).isTrue();
    }

    @Test
    void csrf_required_for_logout() {
        // prime cookies (including XSRF-TOKEN)
        rest.getForEntity("/", String.class);
        ResponseEntity<String> r = rest.postForEntity("/logout", null, String.class);
        // In our config, POST /logout without valid CSRF should be rejected
        org.assertj.core.api.Assertions.assertThat(r.getStatusCode().is4xxClientError())
                .isTrue();
    }
}
