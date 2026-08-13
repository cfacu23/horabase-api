package com.horabase.api.auth;

import com.horabase.api.account.Role;
import org.springframework.security.oauth2.jwt.Jwt;

public record CurrentUser(
        Long accountId,
        Long businessId,
        Long employeeId,
        Role role
) {

    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaim("businessId"),
                jwt.getClaim("employeeId"),
                Role.valueOf(jwt.getClaimAsString("role"))
        );
    }
}
