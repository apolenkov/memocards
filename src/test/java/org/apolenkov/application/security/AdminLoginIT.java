package org.apolenkov.application.security;

import org.apolenkov.application.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev"})
@org.junit.jupiter.api.Tag("integration")
class AdminLoginIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void admin_can_access_admin_page_after_login() {
        // ensure cookies/csrf are initialized
        rest.getForEntity("/login", String.class);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "admin");
        form.add("password", "admin");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> login = rest.postForEntity("/login", new HttpEntity<>(form, headers), String.class);
        org.assertj.core.api.Assertions.assertThat(login.getStatusCode().is3xxRedirection())
                .isTrue();

        ResponseEntity<String> r = rest.getForEntity("/admin/users", String.class);
        org.assertj.core.api.Assertions.assertThat(r.getStatusCode().is2xxSuccessful())
                .isTrue();
    }
}
