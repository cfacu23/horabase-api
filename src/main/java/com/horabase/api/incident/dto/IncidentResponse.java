package com.horabase.api.incident.dto;
import com.horabase.api.incident.*; import java.time.*;
public record IncidentResponse(Long id,Long businessId,Long employeeId,String employeeName,Long shiftId,Long attendanceId,LocalDate incidentDate,IncidentType type,IncidentStatus status,Long detectedMinutes,String notes,OffsetDateTime createdAt,OffsetDateTime updatedAt){}
