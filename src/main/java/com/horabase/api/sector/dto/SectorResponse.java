package com.horabase.api.sector.dto;

import java.time.OffsetDateTime;

public record SectorResponse(
        Long id,
        Long businessId,
        String name,
        String description,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}