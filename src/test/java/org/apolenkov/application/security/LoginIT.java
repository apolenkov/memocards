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
class LoginIT {

    @Autowired
    TestRestTemplate rest;

    @Test
    void user_can_login_and_access_home() {
        // init login page to prime cookies
        rest.getForEntity("/login", String.class);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "user");
        form.add("password", "Password1");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> login = rest.postForEntity("/login", new HttpEntity<>(form, headers), String.class);
        org.assertj.core.api.Assertions.assertThat(login.getStatusCode().is3xxRedirection())
                .isTrue();

        ResponseEntity<String> home = rest.getForEntity("/home", String.class);
        org.assertj.core.api.Assertions.assertThat(home.getStatusCode().is2xxSuccessful())
                .isTrue();
    }
}
