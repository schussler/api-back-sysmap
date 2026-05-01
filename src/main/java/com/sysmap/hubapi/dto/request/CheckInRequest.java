package com.sysmap.hubapi.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(
        @NotBlank(message = "Informe os campos obrigatórios corretamente.") String confirmationCode
) {}
