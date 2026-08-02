package com.horabase.api.business.dto;

import java.time.OffsetDateTime;

public record BusinessResponse(
        Long id,
        String name,
        String taxId,
        String address,
        String phone,
        String email,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}