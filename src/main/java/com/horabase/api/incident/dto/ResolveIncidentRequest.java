package com.horabase.api.incident.dto;
import com.horabase.api.incident.IncidentStatus; import jakarta.validation.constraints.*;
public record ResolveIncidentRequest(@NotNull IncidentStatus status,@Size(max=1000) String notes){}
