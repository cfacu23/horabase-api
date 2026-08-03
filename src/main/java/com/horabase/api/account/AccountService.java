package com.horabase.api.account;

import com.horabase.api.account.dto.AccountResponse;
import com.horabase.api.account.dto.CreateAdminAccountRequest;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            AccountRepository accountRepository,
            BusinessRepository businessRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccountResponse createAdmin(
            Long businessId,
            CreateAdminAccountRequest request
    ) {
        Business business = findBusinessById(businessId);

        if (!business.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pueden crear cuentas en un comercio inactivo"
            );
        }

        String normalizedDocument = normalizeDocument(request.document());
        String normalizedEmail = normalizeEmail(request.email());

        if (normalizedDocument.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cédula no tiene un formato válido"
            );
        }

        if (accountRepository.existsByBusiness_IdAndDocument(
                businessId,
                normalizedDocument
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cuenta con esa cédula"
            );
        }

        if (accountRepository.existsByBusiness_IdAndEmailIgnoreCase(
                businessId,
                normalizedEmail
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese correo"
            );
        }

        Account account = new Account();
        account.setBusiness(business);
        account.setDocument(normalizedDocument);
        account.setEmail(normalizedEmail);
        account.setPasswordHash(
                passwordEncoder.encode(request.temporaryPassword())
        );
        account.setRole(Role.ADMIN);
        account.setActive(true);
        account.setMustChangePassword(true);

        return toResponse(accountRepository.save(account));
    }

    private Business findBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el comercio"
                ));
    }

    private String normalizeDocument(String document) {
        return document.replaceAll("[^0-9]", "");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getBusiness().getId(),
                account.getDocument(),
                account.getEmail(),
                account.getRole(),
                account.isActive(),
                account.isMustChangePassword(),
                account.getLastLoginAt(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}