package com.horabase.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Positive(message = "El identificador del comercio debe ser válido")
        Long businessId,

        @NotBlank(message = "La cédula es obligatoria")
        @Size(max = 20, message = "La cédula no puede superar los 20 caracteres")
        String document,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 72, message = "La contraseña no puede superar los 72 caracteres")
        String password
) {
}
