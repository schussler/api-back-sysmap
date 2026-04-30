package com.sysmap.hubapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "Informe os campos obrigatórios corretamente.") String name,
        @NotBlank(message = "Informe os campos obrigatórios corretamente.") String email,
        @NotBlank(message = "Informe os campos obrigatórios corretamente.") String cpf,
        @NotBlank(message = "Informe os campos obrigatórios corretamente.") String password
) {}
