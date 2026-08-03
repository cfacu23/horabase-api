package com.horabase.api.shift.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateShiftRequest(

        @NotNull(message = "El empleado es obligatorio")
        @Positive(message = "El identificador del empleado debe ser válido")
        Long employeeId,

        @NotNull(message = "El sector es obligatorio")
        @Positive(message = "El identificador del sector debe ser válido")
        Long sectorId,

        @NotNull(message = "La fecha y hora de inicio son obligatorias")
        OffsetDateTime startsAt,

        @NotNull(message = "La fecha y hora de finalización son obligatorias")
        OffsetDateTime endsAt,

        @Min(value = 0, message = "El descanso no puede ser negativo")
        @Max(value = 1440, message = "El descanso no puede superar 1440 minutos")
        int breakMinutes,

        @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
        String notes
) {
}