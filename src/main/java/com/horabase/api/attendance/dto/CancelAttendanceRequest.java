package com.horabase.api.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelAttendanceRequest(
        @NotBlank(message = "El motivo de la anulación es obligatorio")
        @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
        String reason
) {
}
