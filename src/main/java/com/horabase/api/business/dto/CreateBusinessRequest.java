package com.horabase.api.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBusinessRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String name,

        @NotBlank(message = "El RUT es obligatorio")
        @Size(max = 30, message = "El RUT no puede superar los 30 caracteres")
        String taxId,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        String address,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String phone,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email
) {
}