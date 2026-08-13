package com.horabase.api.auth.dto;

import java.time.OffsetDateTime;

public record LoginResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        AuthenticatedUserResponse user
) {
}
