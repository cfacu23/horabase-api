package com.horabase.api.auth;

import com.horabase.api.account.Account;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final String issuer;
    private final long expirationMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            Clock clock,
            @Value("${horabase.jwt.issuer:horabase-api}") String issuer,
            @Value("${horabase.jwt.expiration-minutes:60}")
            long expirationMinutes
    ) {
        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public IssuedToken issue(
            Account account,
            Long employeeId
    ) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plusSeconds(expirationMinutes * 60);

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(account.getId().toString())
                .claim("businessId", account.getBusiness().getId())
                .claim("employeeId", employeeId)
                .claim("document", account.getDocument())
                .claim("role", account.getRole().name());

        if (employeeId != null) {
            builder.claim("employeeId", employeeId);
        }

        JwtClaimsSet claims = builder.build();

        String value = jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();

        return new IssuedToken(
                value,
                OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC)
        );
    }

    public record IssuedToken(
            String value,
            OffsetDateTime expiresAt
    ) {
    }
}
