package com.horabase.api.config;

import com.horabase.api.account.AccountRepository;
import com.horabase.api.account.AccountAccessState;
import com.horabase.api.common.error.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.MediaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
public class ActiveAccountFilter extends OncePerRequestFilter {

    private final AccountRepository accountRepository;
    private final JsonMapper jsonMapper;

    public ActiveAccountFilter(
            AccountRepository accountRepository,
            JsonMapper jsonMapper
    ) {
        this.accountRepository = accountRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication()
                instanceof JwtAuthenticationToken authentication) {
            Jwt jwt = authentication.getToken();
            Long accountId = parseLong(jwt.getSubject());
            Long businessId = jwt.getClaim("businessId");

            AccountAccessState state = accountId == null || businessId == null
                    ? null
                    : accountRepository
                            .findAccessState(accountId, businessId)
                            .orElse(null);

            boolean active = state != null
                    && state.getAccountActive()
                    && state.getBusinessActive();

            if (!active) {
                SecurityContextHolder.clearContext();
            } else if (state.getMustChangePassword()
                    && !request.getRequestURI().equals(
                            "/api/auth/change-password"
                    )) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                jsonMapper.writeValue(
                        response.getOutputStream(),
                        ApiErrorResponse.of(
                                HttpServletResponse.SC_FORBIDDEN,
                                "Forbidden",
                                "Debe cambiar la contraseña antes de continuar",
                                request.getRequestURI()
                        )
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
