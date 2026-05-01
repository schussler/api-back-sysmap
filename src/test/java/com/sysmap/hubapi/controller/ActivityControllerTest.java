package com.sysmap.hubapi.controller;

import com.sysmap.hubapi.dto.request.CheckInRequest;
import com.sysmap.hubapi.dto.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityControllerTest extends AbstractIntegrationTest {

    private String creatorToken;
    private String participantToken;
    private UUID typeId;

    @BeforeEach
    void setUpUsers() {
        String s = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        creatorToken     = signUp("Criador",     "cria" + s + "@t.com", "1" + s + "01", "pass");
        participantToken = signUp("Participante","part" + s + "@t.com", "2" + s + "02", "pass");

        var typesResp = restTemplate.exchange(
                baseUrl("/activities/types"), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(creatorToken)), ActivityTypeResponse[].class);
        typeId = typesResp.getBody()[0].id();
    }

    @Test
    void shouldReturn200WithActivityTypes() {
        var response = restTemplate.exchange(
                baseUrl("/activities/types"), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(creatorToken)), ActivityTypeResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(5);
    }

    @Test
    void shouldReturn200WithPagedActivities() {
        var response = restTemplate.exchange(
                baseUrl("/activities?page=1&pageSize=10"), HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(creatorToken)), PagedActivityResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().page()).isEqualTo(1);
    }

    @Test
    void shouldReturn201AfterCreatingActivity() {
        var activity = createActivity(creatorToken, typeId, false);

        assertThat(activity).isNotNull();
        assertThat(activity.confirmationCode()).isNotBlank();
        assertThat(activity.creator()).isNotNull();
    }

    @Test
    void shouldReturn200AfterSubscribing() {
        var activity = createActivity(creatorToken, typeId, false);

        var response = restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/subscribe"), HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(participantToken)), SubscriptionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().approved()).isTrue();
    }

    @Test
    void shouldReturn409WhenAlreadySubscribed() {
        var activity = createActivity(creatorToken, typeId, false);

        restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/subscribe"), HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(participantToken)), Void.class);

        var response = restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/subscribe"), HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(participantToken)), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturn200AfterCheckIn() {
        var activity = createActivity(creatorToken, typeId, false);

        restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/subscribe"), HttpMethod.POST,
                new HttpEntity<>(bearerHeaders(participantToken)), Void.class);

        var headers = bearerHeaders(participantToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        var response = restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/check-in"), HttpMethod.PUT,
                new HttpEntity<>(new CheckInRequest(activity.confirmationCode()), headers),
                MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().message()).contains("confirmada");
    }

    @Test
    void shouldReturn200AfterConcludingActivity() {
        var activity = createActivity(creatorToken, typeId, false);

        var response = restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/conclude"), HttpMethod.PUT,
                new HttpEntity<>(bearerHeaders(creatorToken)), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturn403WhenNonCreatorTriesToConclude() {
        var activity = createActivity(creatorToken, typeId, false);

        var response = restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/conclude"), HttpMethod.PUT,
                new HttpEntity<>(bearerHeaders(participantToken)), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn200AfterDeletingActivity() {
        var activity = createActivity(creatorToken, typeId, false);

        var response = restTemplate.exchange(
                baseUrl("/activities/" + activity.id() + "/delete"), HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(creatorToken)), MessageResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── helper ───────────────────────────────────────────────────────────

    private ActivityResponse createActivity(String token, UUID activityTypeId, boolean isPrivate) {
        var form = new LinkedMultiValueMap<String, Object>();
        form.add("title",         "Atividade de Teste");
        form.add("description",   "Descrição de teste");
        form.add("typeId",        activityTypeId.toString());
        form.add("address",       "-23.5505,-46.6333");
        form.add("scheduledDate", "2026-12-01T10:00:00");
        form.add("private",       String.valueOf(isPrivate));
        form.add("image",         new ClassPathResource("test-image.png"));

        var headers = bearerHeaders(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var response = restTemplate.exchange(
                baseUrl("/activities/new"), HttpMethod.POST,
                new HttpEntity<>(form, headers), ActivityResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }
}
