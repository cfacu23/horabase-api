package com.horabase.api.terminal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTerminalDeviceRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String name,

        @NotBlank(message = "El identificador es obligatorio")
        @Size(max = 80)
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "El identificador contiene caracteres no permitidos"
        )
        String identifier
) {
}
