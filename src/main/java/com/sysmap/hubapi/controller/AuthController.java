package com.sysmap.hubapi.controller;

import com.sysmap.hubapi.dto.request.RegisterRequest;
import com.sysmap.hubapi.dto.request.SignInRequest;
import com.sysmap.hubapi.dto.response.MessageResponse;
import com.sysmap.hubapi.dto.response.SignInResponse;
import com.sysmap.hubapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios não informados"),
            @ApiResponse(responseCode = "409", description = "E-mail ou CPF já cadastrado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public MessageResponse register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return new MessageResponse("Usuário criado com sucesso.");
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Autenticar usuário e obter token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios não informados"),
            @ApiResponse(responseCode = "401", description = "Senha incorreta"),
            @ApiResponse(responseCode = "403", description = "Conta desativada"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro inesperado")
    })
    public SignInResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authService.signIn(request);
    }
}
