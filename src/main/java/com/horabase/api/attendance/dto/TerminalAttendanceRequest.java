package com.horabase.api.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TerminalAttendanceRequest(
        @NotNull(message = "El empleado es obligatorio")
        @Positive(message = "El identificador del empleado debe ser válido")
        Long employeeId
) {
}
