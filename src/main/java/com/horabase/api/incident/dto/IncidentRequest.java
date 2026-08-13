package com.horabase.api.incident.dto;
import com.horabase.api.incident.*; import jakarta.validation.constraints.*; import java.time.LocalDate;
public record IncidentRequest(@NotNull @Positive Long employeeId,@Positive Long shiftId,@Positive Long attendanceId,@NotNull LocalDate incidentDate,@NotNull IncidentType type,@Positive Long detectedMinutes,@Size(max=1000) String notes){}
