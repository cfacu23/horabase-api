package com.horabase.api.sector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSectorRequest(

        @NotBlank(message = "El nombre del sector es obligatorio")
        @Size(
                max = 80,
                message = "El nombre no puede superar los 80 caracteres"
        )
        String name,

        @Size(
                max = 250,
                message = "La descripción no puede superar los 250 caracteres"
        )
        String description
) {
}