package com.sysmap.hubapi.controller;

import com.sysmap.hubapi.dto.request.UpdateUserRequest;
import com.sysmap.hubapi.dto.response.AvatarResponse;
import com.sysmap.hubapi.dto.response.MessageResponse;
import com.sysmap.hubapi.dto.response.PreferenceResponse;
import com.sysmap.hubapi.dto.response.UserResponse;
import com.sysmap.hubapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuário", description = "Gerenciamento do usuário autenticado")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Retorna dados do usuário logado e conquistas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Conta desativada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public UserResponse getUser() {
        return userService.getAuthenticatedUser();
    }

    @GetMapping("/preferences")
    @Operation(summary = "Retorna interesses do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferências retornadas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Conta desativada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public List<PreferenceResponse> getPreferences() {
        return userService.getPreferences();
    }

    @PostMapping("/preferences/define")
    @Operation(summary = "Define ou substitui todos os interesses do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferências definidas"),
            @ApiResponse(responseCode = "400", description = "IDs de tipo inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Conta desativada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public void definePreferences(@RequestBody List<UUID> typeIds) {
        userService.definePreferences(typeIds);
    }

    @PutMapping("/avatar")
    @Operation(summary = "Atualiza foto de perfil (PNG ou JPG)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avatar atualizado"),
            @ApiResponse(responseCode = "400", description = "Formato de imagem inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Conta desativada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public AvatarResponse updateAvatar(@RequestParam("avatar") MultipartFile file) {
        return userService.updateAvatar(file);
    }

    @PutMapping("/update")
    @Operation(summary = "Atualiza nome, e-mail e/ou senha do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Conta desativada"),
            @ApiResponse(responseCode = "409", description = "E-mail já utilizado por outro usuário"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public UserResponse updateUser(@RequestBody UpdateUserRequest request) {
        return userService.updateUser(request);
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Desativa a conta do usuário (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta desativada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Conta desativada"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public MessageResponse deactivate() {
        userService.deactivate();
        return new MessageResponse("Conta desativada com sucesso.");
    }
}
