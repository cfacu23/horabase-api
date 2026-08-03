package com.horabase.api.account.dto;

import com.horabase.api.account.Role;

import java.time.OffsetDateTime;

public record AccountResponse(
        Long id,
        Long businessId,
        String document,
        String email,
        Role role,
        boolean active,
        boolean mustChangePassword,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}