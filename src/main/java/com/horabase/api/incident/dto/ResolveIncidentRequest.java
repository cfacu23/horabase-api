package com.horabase.api.incident.dto;

import com.horabase.api.incident.IncidentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveIncidentRequest(
        @NotNull IncidentStatus status,
        @Size(max = 1000) String notes
) {
}
