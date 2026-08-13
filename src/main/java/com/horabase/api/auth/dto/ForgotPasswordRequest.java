package com.horabase.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @Positive(message = "El identificador del comercio debe ser válido")
        Long businessId,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150)
        String email
) {
}
