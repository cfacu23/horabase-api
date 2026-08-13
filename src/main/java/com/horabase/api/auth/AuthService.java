package com.horabase.api.auth;

import com.horabase.api.account.Account;
import com.horabase.api.account.AccountRepository;
import com.horabase.api.auth.dto.AuthenticatedUserResponse;
import com.horabase.api.auth.dto.LoginRequest;
import com.horabase.api.auth.dto.LoginResponse;
import com.horabase.api.employee.EmployeeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Clock clock;

    public AuthService(
            AccountRepository accountRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.clock = clock;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String document = normalizeDocument(request.document());
        Account account = findAccountForLogin(
                request.businessId(),
                document
        );

        if (!account.isActive()
                || !account.getBusiness().isActive()
                || !passwordEncoder.matches(
                        request.password(),
                        account.getPasswordHash()
                )) {
            throw invalidCredentials();
        }

        Long employeeId = employeeRepository
                .findByAccount_Id(account.getId())
                .map(employee -> employee.getId())
                .orElse(null);

        account.setLastLoginAt(OffsetDateTime.now(clock));
        accountRepository.save(account);

        JwtService.IssuedToken token = jwtService.issue(
                account,
                employeeId
        );

        return new LoginResponse(
                token.value(),
                "Bearer",
                token.expiresAt(),
                new AuthenticatedUserResponse(
                        account.getId(),
                        account.getBusiness().getId(),
                        employeeId,
                        account.getDocument(),
                        account.getEmail(),
                        account.getRole(),
                        account.isMustChangePassword()
                )
        );
    }

    private Account findAccountForLogin(
            Long businessId,
            String document
    ) {
        if (document.isBlank()) {
            throw invalidCredentials();
        }

        if (businessId != null) {
            return accountRepository
                    .findByBusiness_IdAndDocument(businessId, document)
                    .orElseThrow(this::invalidCredentials);
        }

        List<Account> matches = accountRepository
                .findAllByDocument(document);

        if (matches.size() != 1) {
            if (matches.size() > 1) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Debe indicar el comercio para iniciar sesión"
                );
            }

            throw invalidCredentials();
        }

        return matches.getFirst();
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales inválidas"
        );
    }

    private String normalizeDocument(String document) {
        return document.replaceAll("[^0-9]", "");
    }
}
