package com.horabase.api.incident.dto;

import com.horabase.api.incident.IncidentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record IncidentRequest(
        @NotNull @Positive Long employeeId,
        @Positive Long shiftId,
        @Positive Long attendanceId,
        @NotNull @PastOrPresent LocalDate incidentDate,
        @NotNull IncidentType type,
        @Positive Long detectedMinutes,
        @Size(max = 1000) String notes
) {
}
