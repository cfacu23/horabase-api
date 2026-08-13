package com.horabase.api.auth;

import com.horabase.api.account.Account;
import com.horabase.api.account.AccountRepository;
import com.horabase.api.account.Role;
import com.horabase.api.auth.dto.LoginRequest;
import com.horabase.api.auth.dto.LoginResponse;
import com.horabase.api.business.Business;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;
    private Account account;
    private Employee employee;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                accountRepository,
                employeeRepository,
                passwordEncoder,
                jwtService,
                Clock.fixed(
                        Instant.parse("2026-08-13T14:00:00Z"),
                        ZoneOffset.UTC
                )
        );

        Business business = new Business();
        ReflectionTestUtils.setField(business, "id", 1L);

        account = new Account();
        ReflectionTestUtils.setField(account, "id", 2L);
        account.setBusiness(business);
        account.setDocument("12345678");
        account.setEmail("ana@example.com");
        account.setPasswordHash("encoded");
        account.setRole(Role.EMPLOYEE);
        account.setActive(true);

        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", 3L);
    }

    @Test
    void loginReturnsTenantAndEmployeeClaims() {
        when(accountRepository.findByBusiness_IdAndDocument(1L, "12345678"))
                .thenReturn(Optional.of(account));
        when(employeeRepository.findByAccount_Id(2L))
                .thenReturn(Optional.of(employee));
        when(passwordEncoder.matches("password", "encoded"))
                .thenReturn(true);
        when(jwtService.issue(account, 3L))
                .thenReturn(new JwtService.IssuedToken(
                        "token",
                        java.time.OffsetDateTime.parse(
                                "2026-08-13T15:00:00Z"
                        )
                ));

        LoginResponse response = authService.login(
                new LoginRequest(1L, "12.345.678", "password")
        );

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.user().businessId()).isEqualTo(1L);
        assertThat(response.user().employeeId()).isEqualTo(3L);
        assertThat(response.user().role()).isEqualTo(Role.EMPLOYEE);
        verify(accountRepository).save(account);
    }

    @Test
    void loginDoesNotRevealWhetherPasswordOrAccountFailed() {
        when(accountRepository.findByBusiness_IdAndDocument(1L, "12345678"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "encoded"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest(1L, "12345678", "wrong")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    void loginRequiresBusinessWhenDocumentIsAmbiguous() {
        Account other = new Account();
        when(accountRepository.findAllByDocument("12345678"))
                .thenReturn(List.of(account, other));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest(null, "12345678", "password")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Debe indicar el comercio");
    }
}
