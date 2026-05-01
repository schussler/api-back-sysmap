package com.sysmap.hubapi.controller;

import com.sysmap.hubapi.dto.request.RegisterRequest;
import com.sysmap.hubapi.dto.request.SignInRequest;
import com.sysmap.hubapi.dto.response.MessageResponse;
import com.sysmap.hubapi.dto.response.SignInResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest extends AbstractIntegrationTest {

    // Sufixo único por execução para evitar colisão com dados existentes no banco
    private final String s = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    private String email(String prefix) { return prefix + s + "@test.com"; }
    private String cpf(String digits)   { return digits.substring(0, 3) + "." + digits.substring(3, 6)
                                               + "." + digits.substring(6, 9) + "-" + s.substring(0, 2); }

    @Test
    void shouldReturn201WhenRegisterIsSuccessful() {
        var response = restTemplate.postForEntity(
                baseUrl("/auth/register"),
                new RegisterRequest("Ana", email("ana"), cpf("111111111"), "senha123"),
                MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().message()).isEqualTo("Usuário criado com sucesso.");
    }

    @Test
    void shouldReturn400WhenRegisterFieldsAreMissing() {
        var response = restTemplate.postForEntity(
                baseUrl("/auth/register"),
                new RegisterRequest("", "", "", ""),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() {
        restTemplate.postForEntity(baseUrl("/auth/register"),
                new RegisterRequest("Bruno", email("bruno"), cpf("222222222"), "senha123"),
                Void.class);

        var response = restTemplate.postForEntity(
                baseUrl("/auth/register"),
                new RegisterRequest("Bruno2", email("bruno"), cpf("333333333"), "senha123"),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturn409WhenCpfAlreadyExists() {
        String sharedCpf = cpf("444444444");
        restTemplate.postForEntity(baseUrl("/auth/register"),
                new RegisterRequest("Carlos", email("carlos"), sharedCpf, "senha123"),
                Void.class);

        var response = restTemplate.postForEntity(
                baseUrl("/auth/register"),
                new RegisterRequest("Carlos2", email("carlos2"), sharedCpf, "senha123"),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturn200WithTokenOnSignIn() {
        String e = email("diana");
        restTemplate.postForEntity(baseUrl("/auth/register"),
                new RegisterRequest("Diana", e, cpf("555555555"), "senha123"),
                Void.class);

        var response = restTemplate.postForEntity(
                baseUrl("/auth/sign-in"),
                new SignInRequest(e, "senha123"),
                SignInResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().email()).isEqualTo(e);
    }

    @Test
    void shouldReturn401WhenPasswordIsWrong() {
        String e = email("eva");
        restTemplate.postForEntity(baseUrl("/auth/register"),
                new RegisterRequest("Eva", e, cpf("666666666"), "correta"),
                Void.class);

        var response = restTemplate.postForEntity(
                baseUrl("/auth/sign-in"),
                new SignInRequest(e, "errada"),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturn404WhenUserNotFound() {
        var response = restTemplate.postForEntity(
                baseUrl("/auth/sign-in"),
                new SignInRequest("naoexiste" + s + "@test.com", "senha"),
                Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
