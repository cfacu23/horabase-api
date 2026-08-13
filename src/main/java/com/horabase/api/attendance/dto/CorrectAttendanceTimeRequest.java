package com.horabase.api.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CorrectAttendanceTimeRequest(
        @NotNull(message = "La fecha y hora son obligatorias")
        OffsetDateTime occurredAt,

        @NotBlank(message = "El motivo de la corrección es obligatorio")
        @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
        String reason
) {
}
