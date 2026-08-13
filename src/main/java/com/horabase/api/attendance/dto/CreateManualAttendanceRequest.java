package com.horabase.api.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateManualAttendanceRequest(
        @NotNull(message = "El empleado es obligatorio")
        @Positive(message = "El identificador del empleado debe ser válido")
        Long employeeId,

        @Positive(message = "El identificador del turno debe ser válido")
        Long shiftId,

        @NotNull(message = "La entrada es obligatoria")
        OffsetDateTime checkInAt,

        OffsetDateTime checkOutAt,

        @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
        String notes
) {
}
