package com.sysmap.hubapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInRequest(

        @NotBlank(message = "Informe os campos obrigatórios corretamente.")
        @Email(message = "Informe os campos obrigatórios corretamente.")
        String email,

        @NotBlank(message = "Informe os campos obrigatórios corretamente.")
        String password

) {}
