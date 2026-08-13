package com.horabase.api.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("businessSecurity")
public class BusinessSecurity {

    public boolean canAccess(
            Authentication authentication,
            Long businessId
    ) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
                || businessId == null) {
            return false;
        }

        Long authenticatedBusinessId = jwtAuth.getToken()
                .getClaim("businessId");

        return businessId.equals(authenticatedBusinessId);
    }
}
