package com.horabase.api.shift.dto;

import com.horabase.api.shift.ShiftStatus;
import jakarta.validation.constraints.*;

import java.time.OffsetDateTime;

public record UpdateShiftRequest(

        @NotNull(message = "El empleado es obligatorio")
        @Positive
        Long employeeId,

        @NotNull(message = "El sector es obligatorio")
        @Positive
        Long sectorId,

        @NotNull(message = "La fecha y hora de inicio son obligatorias")
        OffsetDateTime startsAt,

        @NotNull(message = "La fecha y hora de finalización son obligatorias")
        OffsetDateTime endsAt,

        @Min(0)
        @Max(1440)
        int breakMinutes,

        @Size(max = 500)
        String notes,

        @NotNull(message = "El estado es obligatorio")
        ShiftStatus status
) {
}