package com.sysmap.hubapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(min = 2, max = 100, message = "Informe os campos obrigatórios corretamente.")
        String name,

        @Email(message = "Informe os campos obrigatórios corretamente.")
        String email,

        @Size(min = 8, message = "Informe os campos obrigatórios corretamente.")
        String password

) {}
