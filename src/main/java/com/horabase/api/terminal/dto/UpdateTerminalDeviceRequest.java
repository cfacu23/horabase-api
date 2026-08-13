package com.horabase.api.terminal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTerminalDeviceRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String name,

        boolean active
) {
}
