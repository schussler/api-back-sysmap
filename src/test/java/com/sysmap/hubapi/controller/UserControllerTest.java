package com.sysmap.hubapi.controller;

import com.sysmap.hubapi.dto.request.UpdateUserRequest;
import com.sysmap.hubapi.dto.response.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserControllerTest extends AbstractIntegrationTest {

    private final String s = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    private String email(String p) { return p + s + "@test.com"; }
    private String cpf(String variant) {
        long base = Long.parseUnsignedLong(s.substring(0, 8), 16);
        long hash = Math.abs((long) variant.hashCode()); // always non-negative
        long v = base + hash;
        return String.format("%03d.%03d.%03d-%02d",
                v % 1000, (v >> 10) % 1000, (v >> 20) % 1000, (v >> 30) % 100);
    }

    @Test
    void shouldReturn200WithUserData() {
        String token = signUp("Fábio", email("fabio"), cpf("101101101"), "senha123");

        var response = restTemplate.exchange(
                baseUrl("/user"), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), UserResponse.class);

        String expectedEmail = email("fabio");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().email()).isEqualTo(expectedEmail);
        assertThat(response.getBody().achievements()).isNotNull();
    }

    @Test
    void shouldReturn403WhenNoToken() {
        var response = restTemplate.getForEntity(baseUrl("/user"), Object.class);
        assertThat(response.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void shouldReturn200WithUserPreferences() {
        String token = signUp("Gabi", email("gabi"), cpf("202202202"), "senha123");

        var response = restTemplate.exchange(
                baseUrl("/user/preferences"), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), List.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldReturn200AfterDefiningPreferences() {
        String token = signUp("Hana", email("hana"), cpf("303303303"), "senha123");

        var typesResp = restTemplate.exchange(
                baseUrl("/activities/types"), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), ActivityTypeResponse[].class);
        UUID typeId = typesResp.getBody()[0].id();

        var headers = bearerHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.exchange(
                baseUrl("/user/preferences/define"), HttpMethod.POST,
                new HttpEntity<>(List.of(typeId.toString()), headers), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturn200AfterUpdatingAvatar() {
        String token = signUp("Iris", email("iris"), cpf("404404404"), "senha123");

        var form = new LinkedMultiValueMap<String, Object>();
        form.add("avatar", new ClassPathResource("test-image.png"));

        var headers = bearerHeaders(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var response = restTemplate.exchange(
                baseUrl("/user/avatar"), HttpMethod.PUT,
                new HttpEntity<>(form, headers), AvatarResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().avatar()).contains("platform-images");
    }

    @Test
    void shouldReturn200AfterUpdatingUser() {
        String token = signUp("João", email("joao"), cpf("505505505"), "senha123");

        var headers = bearerHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.exchange(
                baseUrl("/user/update"), HttpMethod.PUT,
                new HttpEntity<>(new UpdateUserRequest("João Atualizado", null, null), headers),
                UserResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().name()).isEqualTo("João Atualizado");
    }

    @Test
    void shouldReturn403AfterDeactivatingAccount() {
        String token = signUp("Kaká", email("kaka"), cpf("606606606"), "senha123");

        var deactivateResp = restTemplate.exchange(
                baseUrl("/user/deactivate"), HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(token)), MessageResponse.class);
        assertThat(deactivateResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        var userResp = restTemplate.exchange(
                baseUrl("/user"), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)), Object.class);
        assertThat(userResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
