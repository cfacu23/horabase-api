package com.horabase.api.auth;

import com.horabase.api.account.Account;
import com.horabase.api.account.AccountRepository;
import com.horabase.api.account.Role;
import com.horabase.api.auth.dto.ChangePasswordRequest;
import com.horabase.api.auth.dto.ResetPasswordRequest;
import com.horabase.api.business.Business;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordResetEmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordService passwordService;
    private Account account;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService(
                accountRepository,
                tokenRepository,
                emailSender,
                passwordEncoder,
                Clock.fixed(
                        Instant.parse("2026-08-13T14:00:00Z"),
                        ZoneOffset.UTC
                ),
                30,
                "https://app.example/reset"
        );

        Business business = new Business();
        account = new Account();
        ReflectionTestUtils.setField(account, "id", 2L);
        account.setBusiness(business);
        account.setPasswordHash("old-hash");
        account.setActive(true);
        account.setMustChangePassword(true);
    }

    @Test
    void changePasswordValidatesOldPasswordAndClearsFlag() {
        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("old-password", "old-hash"))
                .thenReturn(true);
        when(passwordEncoder.matches("new-password", "old-hash"))
                .thenReturn(false);
        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-hash");

        passwordService.changePassword(
                new CurrentUser(2L, 1L, 3L, Role.EMPLOYEE),
                new ChangePasswordRequest(
                        "old-password",
                        "new-password"
                )
        );

        assertThat(account.getPasswordHash()).isEqualTo("new-hash");
        assertThat(account.isMustChangePassword()).isFalse();
    }

    @Test
    void resetPasswordConsumesToken() {
        PasswordResetToken token = new PasswordResetToken();
        token.setAccount(account);
        token.setExpiresAt(OffsetDateTime.parse("2026-08-13T14:30:00Z"));

        when(tokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password"))
                .thenReturn("new-hash");

        passwordService.resetPassword(
                new ResetPasswordRequest("raw-token", "new-password")
        );

        assertThat(token.getUsedAt())
                .isEqualTo(OffsetDateTime.parse("2026-08-13T14:00:00Z"));
        assertThat(account.getPasswordHash()).isEqualTo("new-hash");
        verify(tokenRepository).save(token);
    }
}
