package org.apolenkov.application.security;

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
class AdminAccessIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void user_forbidden_on_admin() {
        ResponseEntity<String> r = rest.getForEntity("/admin/users", String.class);
        boolean redirected = r.getStatusCode().is3xxRedirection();
        boolean loginContent = r.getBody() != null && r.getBody().toLowerCase().contains("vaadin-login");
        boolean forbidden = r.getStatusCode().value() == 403;
        org.assertj.core.api.Assertions.assertThat(redirected || loginContent || forbidden)
                .isTrue();
    }
}
