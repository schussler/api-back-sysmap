package com.sysmap.hubapi.controller;

import com.sysmap.hubapi.dto.request.ApproveRequest;
import com.sysmap.hubapi.dto.request.CheckInRequest;
import com.sysmap.hubapi.dto.response.*;
import com.sysmap.hubapi.service.ActivityService;
import com.sysmap.hubapi.service.ParticipantService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/activities")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Atividades", description = "Gerenciamento de atividades")
public class ActivityController {

    private final ActivityService activityService;
    private final ParticipantService participantService;

    public ActivityController(ActivityService activityService, ParticipantService participantService) {
        this.activityService = activityService;
        this.participantService = participantService;
    }

    @GetMapping("/types")
    @Operation(summary = "Lista todos os tipos de atividade")
    public List<ActivityTypeResponse> listTypes() {
        return activityService.listTypes();
    }

    @GetMapping
    @Operation(summary = "Lista atividades disponíveis (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public PagedActivityResponse listActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) UUID typeId,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String order) {
        return activityService.listActivities(page, pageSize, typeId, orderBy, order);
    }

    @GetMapping("/all")
    @Operation(summary = "Lista todas as atividades disponíveis (sem paginação)")
    public List<ActivityResponse> listAll(
            @RequestParam(required = false) UUID typeId,
            @RequestParam(defaultValue = "createdAt") String orderBy,
            @RequestParam(defaultValue = "desc") String order) {
        return activityService.listAllActivities(typeId, orderBy, order);
    }

    @GetMapping("/user/creator")
    @Operation(summary = "Lista atividades criadas pelo usuário logado (paginado)")
    public PagedActivityResponse listCreatedByUser(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return activityService.listCreatedByUser(page, pageSize);
    }

    @GetMapping("/user/creator/all")
    @Operation(summary = "Lista todas as atividades criadas pelo usuário logado")
    public List<ActivityResponse> listAllCreatedByUser() {
        return activityService.listAllCreatedByUser();
    }

    @GetMapping("/user/participant")
    @Operation(summary = "Lista atividades em que o usuário logado participa (paginado)")
    public PagedActivityResponse listParticipatedByUser(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return activityService.listParticipatedByUser(page, pageSize);
    }

    @GetMapping("/user/participant/all")
    @Operation(summary = "Lista todas as atividades em que o usuário logado participa")
    public List<ActivityResponse> listAllParticipatedByUser() {
        return activityService.listAllParticipatedByUser();
    }

    @GetMapping("/{id}/participants")
    @Operation(summary = "Lista participantes de uma atividade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participantes retornados"),
            @ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public List<ParticipantResponse> listParticipants(@PathVariable UUID id) {
        return activityService.listParticipants(id);
    }

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma nova atividade")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Atividade criada"),
            @ApiResponse(responseCode = "400", description = "Campos inválidos ou imagem inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public ActivityResponse createActivity(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam UUID typeId,
            @RequestParam String address,
            @RequestParam MultipartFile image,
            @RequestParam String scheduledDate,
            @RequestParam(name = "private") boolean isPrivate) {
        return activityService.createActivity(
                title, description, typeId, address, image,
                LocalDateTime.parse(scheduledDate), isPrivate);
    }

    @PutMapping(value = "/{id}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Edita uma atividade existente (apenas o criador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atividade atualizada"),
            @ApiResponse(responseCode = "403", description = "Não é o criador"),
            @ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public ActivityResponse updateActivity(
            @PathVariable UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UUID typeId,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String scheduledDate,
            @RequestParam(name = "private", required = false) Boolean isPrivate) {
        return activityService.updateActivity(id, title, description, typeId, address,
                image, scheduledDate, isPrivate);
    }

    @PutMapping("/{id}/conclude")
    @Operation(summary = "Conclui a atividade (apenas o criador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atividade concluída"),
            @ApiResponse(responseCode = "403", description = "Não é o criador"),
            @ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public MessageResponse concludeActivity(@PathVariable UUID id) {
        activityService.concludeActivity(id);
        return new MessageResponse("Atividade concluída com sucesso.");
    }

    @DeleteMapping("/{id}/delete")
    @Operation(summary = "Exclui (soft delete) a atividade (apenas o criador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atividade excluída"),
            @ApiResponse(responseCode = "403", description = "Não é o criador"),
            @ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public MessageResponse deleteActivity(@PathVariable UUID id) {
        activityService.deleteActivity(id);
        return new MessageResponse("Atividade excluída com sucesso.");
    }

    @PostMapping("/{id}/subscribe")
    @Operation(summary = "Inscreve o usuário logado na atividade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscrição realizada"),
            @ApiResponse(responseCode = "403", description = "Criador não pode se inscrever"),
            @ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já inscrito ou atividade concluída"),
            @ApiResponse(responseCode = "422", description = "Atividade concluída"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public SubscriptionResponse subscribe(@PathVariable UUID id) {
        return participantService.subscribe(id);
    }

    @DeleteMapping("/{id}/unsubscribe")
    @Operation(summary = "Cancela inscrição do usuário logado na atividade")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscrição cancelada"),
            @ApiResponse(responseCode = "404", description = "Atividade ou inscrição não encontrada"),
            @ApiResponse(responseCode = "422", description = "Check-in já confirmado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public MessageResponse unsubscribe(@PathVariable UUID id) {
        participantService.unsubscribe(id);
        return new MessageResponse("Participação cancelada com sucesso.");
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Aprova ou nega inscrição em atividade privada (apenas o criador)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscrição atualizada"),
            @ApiResponse(responseCode = "403", description = "Não é o criador"),
            @ApiResponse(responseCode = "404", description = "Atividade ou participante não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public void approve(@PathVariable UUID id, @RequestBody ApproveRequest request) {
        participantService.approve(id, request);
    }

    @PutMapping("/{id}/check-in")
    @Operation(summary = "Confirma presença na atividade com código")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presença confirmada"),
            @ApiResponse(responseCode = "400", description = "Código incorreto ou inválido"),
            @ApiResponse(responseCode = "403", description = "Participante não aprovado"),
            @ApiResponse(responseCode = "404", description = "Atividade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Check-in já realizado"),
            @ApiResponse(responseCode = "422", description = "Atividade concluída"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public MessageResponse checkIn(@PathVariable UUID id, @RequestBody @Valid CheckInRequest request) {
        participantService.checkIn(id, request);
        return new MessageResponse("Participação confirmada com sucesso.");
    }
}
