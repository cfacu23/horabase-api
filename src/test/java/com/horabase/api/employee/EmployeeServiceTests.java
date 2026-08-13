package com.horabase.api.employee;

import com.horabase.api.account.Account;
import com.horabase.api.account.AccountRepository;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.dto.CreateEmployeeRequest;
import com.horabase.api.sector.Sector;
import com.horabase.api.sector.SectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTests {

    @Mock EmployeeRepository employees;
    @Mock AccountRepository accounts;
    @Mock BusinessRepository businesses;
    @Mock SectorRepository sectors;

    private final PasswordEncoder encoder =
            PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private EmployeeService service;
    private Business business;
    private Sector sector;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(
                employees, accounts, businesses, sectors, encoder
        );
        business = new Business();
        ReflectionTestUtils.setField(business, "id", 1L);
        business.setActive(true);
        sector = new Sector();
        ReflectionTestUtils.setField(sector, "id", 2L);
        sector.setBusiness(business);
        sector.setName("Ventas");
        sector.setActive(true);
    }

    @Test
    void rejectsDuplicateDocument() {
        stubBusinessAndSector();
        when(accounts.existsByBusiness_IdAndDocument(1L, "45678901"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, request()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("c\u00e9dula");
        verify(accounts, never()).save(any());
    }

    @Test
    void rejectsDuplicateEmail() {
        stubBusinessAndSector();
        when(accounts.existsByBusiness_IdAndEmailIgnoreCase(
                1L, "ana@example.com"
        )).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, request()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("correo");
        verify(accounts, never()).save(any());
    }

    @Test
    void storesEncodedPasswordAndForcesInitialChange() {
        stubBusinessAndSector();
        when(accounts.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            ReflectionTestUtils.setField(account, "id", 3L);
            return account;
        });
        when(employees.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            ReflectionTestUtils.setField(employee, "id", 4L);
            return employee;
        });

        service.create(1L, request());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isNotEqualTo("Temporal123!");
        assertThat(encoder.matches("Temporal123!", saved.getPasswordHash()))
                .isTrue();
        assertThat(saved.isMustChangePassword()).isTrue();
    }

    @Test
    void rejectsSectorFromAnotherBusiness() {
        when(businesses.findById(1L)).thenReturn(Optional.of(business));
        when(sectors.findByIdAndBusiness_Id(2L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(1L, request()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("sector");
    }

    private void stubBusinessAndSector() {
        when(businesses.findById(1L)).thenReturn(Optional.of(business));
        when(sectors.findByIdAndBusiness_Id(2L, 1L))
                .thenReturn(Optional.of(sector));
    }

    private CreateEmployeeRequest request() {
        return new CreateEmployeeRequest(
                "Ana", "P\u00e9rez", "4.567.890-1",
                "ANA@EXAMPLE.COM", "Temporal123!", null, 2L,
                LocalDate.of(2024, 1, 1),
                new BigDecimal("300.00"),
                new BigDecimal("600.00"),
                new BigDecimal("40.00")
        );
    }
}
