package com.horabase.api.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBusinessRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120)
        String name,

        @Size(max = 150)
        String address,

        @Size(max = 30)
        String phone,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150)
        String email,

        boolean active
) {
}