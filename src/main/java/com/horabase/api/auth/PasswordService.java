package com.horabase.api.auth;

import com.horabase.api.account.Account;
import com.horabase.api.account.AccountRepository;
import com.horabase.api.auth.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class PasswordService {

    private static final String GENERIC_FORGOT_MESSAGE =
            "Si existe una cuenta activa, se enviaron instrucciones al correo";

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordResetEmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SecureRandom secureRandom;
    private final long expirationMinutes;
    private final String resetBaseUrl;

    public PasswordService(
            AccountRepository accountRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordResetEmailSender emailSender,
            PasswordEncoder passwordEncoder,
            Clock clock,
            @Value("${horabase.password-reset.expiration-minutes:30}")
            long expirationMinutes,
            @Value("${horabase.password-reset.base-url}")
            String resetBaseUrl
    ) {
        this.accountRepository = accountRepository;
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.secureRandom = new SecureRandom();
        this.expirationMinutes = expirationMinutes;
        this.resetBaseUrl = resetBaseUrl;
    }

    @Transactional
    public MessageResponse changePassword(
            CurrentUser currentUser,
            ChangePasswordRequest request
    ) {
        Account account = findActiveAccount(currentUser.accountId());

        if (!passwordEncoder.matches(
                request.currentPassword(),
                account.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña actual no es correcta"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                account.getPasswordHash()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La nueva contraseña debe ser diferente"
            );
        }

        account.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );
        account.setMustChangePassword(false);
        accountRepository.save(account);

        return new MessageResponse("Contraseña actualizada correctamente");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        Account account = findAccountForReset(
                request.businessId(),
                normalizeEmail(request.email())
        );

        if (account == null
                || !account.isActive()
                || !account.getBusiness().isActive()) {
            return new MessageResponse(GENERIC_FORGOT_MESSAGE);
        }

        tokenRepository.deleteAllByAccount_IdAndUsedAtIsNull(
                account.getId()
        );

        String rawToken = generateToken();
        PasswordResetToken token = new PasswordResetToken();
        token.setAccount(account);
        token.setTokenHash(hash(rawToken));
        token.setExpiresAt(
                OffsetDateTime.now(clock).plusMinutes(expirationMinutes)
        );
        tokenRepository.save(token);

        String separator = resetBaseUrl.contains("?") ? "&" : "?";
        emailSender.sendPasswordReset(
                account,
                resetBaseUrl + separator + "token=" + rawToken
        );

        return new MessageResponse(GENERIC_FORGOT_MESSAGE);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        PasswordResetToken token = tokenRepository
                .findByTokenHash(hash(request.token()))
                .filter(candidate -> candidate.getUsedAt() == null)
                .filter(candidate -> candidate.getExpiresAt().isAfter(now))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El token es inválido o expiró"
                ));

        Account account = token.getAccount();

        if (!account.isActive() || !account.getBusiness().isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El token es inválido o expiró"
            );
        }

        account.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );
        account.setMustChangePassword(false);
        token.setUsedAt(now);
        accountRepository.save(account);
        tokenRepository.save(token);

        return new MessageResponse("Contraseña restablecida correctamente");
    }

    private Account findActiveAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .filter(Account::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "La cuenta no está activa"
                ));
    }

    private Account findAccountForReset(Long businessId, String email) {
        if (businessId != null) {
            return accountRepository
                    .findByBusiness_IdAndEmailIgnoreCase(businessId, email)
                    .orElse(null);
        }

        List<Account> matches = accountRepository
                .findAllByEmailIgnoreCase(email);
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
