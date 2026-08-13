package com.horabase.api.auth.dto;

import com.horabase.api.account.Role;

public record AuthenticatedUserResponse(
        Long accountId,
        Long businessId,
        Long employeeId,
        String document,
        String email,
        Role role,
        boolean mustChangePassword
) {
}
