package com.sysmap.hubapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Informe os campos obrigatórios corretamente.")
        @Size(min = 2, max = 100, message = "Informe os campos obrigatórios corretamente.")
        String name,

        @NotBlank(message = "Informe os campos obrigatórios corretamente.")
        @Email(message = "Informe os campos obrigatórios corretamente.")
        String email,

        @NotBlank(message = "Informe os campos obrigatórios corretamente.")
        @Pattern(
            regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$",
            message = "Informe os campos obrigatórios corretamente."
        )
        String cpf,

        @NotBlank(message = "Informe os campos obrigatórios corretamente.")
        @Size(min = 8, message = "Informe os campos obrigatórios corretamente.")
        String password

) {}
