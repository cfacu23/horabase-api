package com.horabase.api.auth;

import com.horabase.api.account.Account;
import com.horabase.api.account.Role;
import com.horabase.api.business.Business;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTests {

    @Test
    void adminTokenOmitsNullableEmployeeClaim() {
        CapturingEncoder encoder = new CapturingEncoder();
        JwtService service = new JwtService(
                encoder,
                Clock.fixed(Instant.parse("2026-08-13T12:00:00Z"), ZoneOffset.UTC),
                "horabase-api",
                60
        );

        Business business = new Business();
        ReflectionTestUtils.setField(business, "id", 1L);
        Account account = new Account();
        ReflectionTestUtils.setField(account, "id", 2L);
        account.setBusiness(business);
        account.setDocument("45678901");
        account.setRole(Role.ADMIN);

        service.issue(account, null);

        assertThat(encoder.claims).containsEntry("businessId", 1L);
        assertThat(encoder.claims).doesNotContainKey("employeeId");
    }

    private static final class CapturingEncoder implements JwtEncoder {
        private Map<String, Object> claims;

        @Override
        public Jwt encode(JwtEncoderParameters parameters) {
            claims = parameters.getClaims().getClaims();
            Instant issuedAt = parameters.getClaims().getIssuedAt();
            Instant expiresAt = parameters.getClaims().getExpiresAt();
            return new Jwt(
                    "token", issuedAt, expiresAt,
                    Map.of("alg", "HS256"), claims
            );
        }
    }
}
