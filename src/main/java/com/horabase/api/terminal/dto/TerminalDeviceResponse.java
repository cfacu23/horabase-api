package com.horabase.api.terminal.dto;

import java.time.OffsetDateTime;

public record TerminalDeviceResponse(
        Long id,
        Long businessId,
        String name,
        String identifier,
        boolean active,
        OffsetDateTime lastSeenAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
