package com.horabase.api.config;

import com.horabase.api.common.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import com.horabase.api.terminal.TerminalAuthenticationFilter;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${horabase.jwt.secret}") String encodedSecret
    ) {
        byte[] secret;

        try {
            secret = Base64.getDecoder().decode(encodedSecret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET debe estar codificado en Base64",
                    exception
            );
        }

        if (secret.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe contener al menos 32 bytes"
            );
        }

        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey,
            @Value("${horabase.jwt.issuer:horabase-api}") String issuer
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(issuer)
        );
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            String role = jwt.getClaimAsString("role");
            return role == null
                    ? List.of()
                    : List.of(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            ActiveAccountFilter activeAccountFilter,
            TerminalAuthenticationFilter terminalAuthenticationFilter,
            JsonMapper jsonMapper
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/health",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/terminal/**").permitAll()
                        .requestMatchers("/api/businesses/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter
                        ))
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(
                                        response,
                                        jsonMapper,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized",
                                        "Se requiere autenticación",
                                        request.getRequestURI()
                                )
                        )
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(
                                        response,
                                        jsonMapper,
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden",
                                        "No tiene permisos para realizar esta operación",
                                        request.getRequestURI()
                                )
                        )
                )
                .addFilterAfter(
                        activeAccountFilter,
                        BearerTokenAuthenticationFilter.class
                )
                .addFilterBefore(
                        terminalAuthenticationFilter,
                        BearerTokenAuthenticationFilter.class
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    private void writeError(
            HttpServletResponse response,
            JsonMapper jsonMapper,
            int status,
            String error,
            String message,
            String path
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(
                response.getOutputStream(),
                new ApiErrorResponse(
                        OffsetDateTime.now(),
                        status,
                        error,
                        message,
                        path,
                        null
                )
        );
    }
}
