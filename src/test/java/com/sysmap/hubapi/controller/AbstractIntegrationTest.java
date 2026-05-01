package com.sysmap.hubapi.controller;

import com.sysmap.hubapi.dto.request.RegisterRequest;
import com.sysmap.hubapi.dto.request.SignInRequest;
import com.sysmap.hubapi.dto.response.SignInResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected RestTemplate restTemplate;

    protected String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    protected String signUp(String name, String email, String cpf, String password) {
        restTemplate.postForEntity(
                baseUrl("/auth/register"),
                new RegisterRequest(name, email, cpf, password),
                Void.class);
        var resp = restTemplate.postForEntity(
                baseUrl("/auth/sign-in"),
                new SignInRequest(email, password),
                SignInResponse.class);
        return resp.getBody().token();
    }

    protected HttpHeaders bearerHeaders(String token) {
        var h = new HttpHeaders();
        h.setBearerAuth(token);
        return h;
    }
}
