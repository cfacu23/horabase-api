package com.horabase.api.config;

import com.horabase.api.account.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ActiveAccountFilter extends OncePerRequestFilter {

    private final AccountRepository accountRepository;

    public ActiveAccountFilter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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

            boolean active = accountId != null
                    && businessId != null
                    && accountRepository
                    .existsByIdAndActiveTrueAndBusiness_IdAndBusiness_ActiveTrue(
                            accountId,
                            businessId
                    );

            if (!active) {
                SecurityContextHolder.clearContext();
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
