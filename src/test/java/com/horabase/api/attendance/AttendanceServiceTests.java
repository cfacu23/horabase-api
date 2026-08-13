package com.horabase.api.attendance;

import com.horabase.api.account.Account;
import com.horabase.api.attendance.dto.AttendanceResponse;
import com.horabase.api.attendance.dto.TerminalAttendanceRequest;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.shift.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTests {

    private static final Long BUSINESS_ID = 1L;
    private static final Long EMPLOYEE_ID = 10L;
    private static final Instant NOW = Instant.parse("2026-08-13T13:00:00Z");

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ShiftRepository shiftRepository;

    private AttendanceService attendanceService;
    private Business business;
    private Employee employee;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        attendanceService = new AttendanceService(
                attendanceRepository,
                businessRepository,
                employeeRepository,
                shiftRepository,
                clock,
                240
        );

        business = new Business();
        ReflectionTestUtils.setField(business, "id", BUSINESS_ID);

        Account account = new Account();
        account.setActive(true);

        employee = new Employee();
        ReflectionTestUtils.setField(employee, "id", EMPLOYEE_ID);
        employee.setBusiness(business);
        employee.setAccount(account);
        employee.setFirstName("Ana");
        employee.setLastName("Pérez");

        when(businessRepository.findById(BUSINESS_ID))
                .thenReturn(Optional.of(business));
        when(employeeRepository.findByIdAndBusinessIdForUpdate(
                EMPLOYEE_ID,
                BUSINESS_ID
        )).thenReturn(Optional.of(employee));
    }

    @Test
    void checkInUsesServerTimeAndCreatesOpenAttendance() {
        when(attendanceRepository
                .existsByBusiness_IdAndEmployee_IdAndStatus(
                        BUSINESS_ID,
                        EMPLOYEE_ID,
                        AttendanceStatus.OPEN
                )).thenReturn(false);
        when(shiftRepository
                .findAllByBusiness_IdAndEmployee_IdAndStatusAndStartsAtBetweenOrderByStartsAtAsc(
                        any(), any(), any(), any(), any()
                )).thenReturn(List.of());
        when(attendanceRepository.save(any(Attendance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceResponse response = attendanceService.checkIn(
                BUSINESS_ID,
                new TerminalAttendanceRequest(EMPLOYEE_ID)
        );

        assertThat(response.checkInAt())
                .isEqualTo(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
        assertThat(response.status()).isEqualTo(AttendanceStatus.OPEN);
        assertThat(response.origin()).isEqualTo(AttendanceOrigin.TERMINAL);
        assertThat(response.shiftId()).isNull();
        assertThat(response.checkOutAt()).isNull();
    }

    @Test
    void checkInRejectsSecondOpenAttendance() {
        when(attendanceRepository
                .existsByBusiness_IdAndEmployee_IdAndStatus(
                        BUSINESS_ID,
                        EMPLOYEE_ID,
                        AttendanceStatus.OPEN
                )).thenReturn(true);

        assertThatThrownBy(() -> attendanceService.checkIn(
                BUSINESS_ID,
                new TerminalAttendanceRequest(EMPLOYEE_ID)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("ya tiene una asistencia abierta");
    }

    @Test
    void checkOutRejectsEmployeeWithoutOpenAttendance() {
        when(attendanceRepository
                .findFirstByBusiness_IdAndEmployee_IdAndStatusOrderByCheckInAtDesc(
                        BUSINESS_ID,
                        EMPLOYEE_ID,
                        AttendanceStatus.OPEN
                )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.checkOut(
                BUSINESS_ID,
                new TerminalAttendanceRequest(EMPLOYEE_ID)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("no tiene una asistencia abierta");
    }

    @Test
    void checkOutClosesAttendanceAndCalculatesWorkedMinutes() {
        Attendance attendance = new Attendance();
        attendance.setBusiness(business);
        attendance.setEmployee(employee);
        attendance.setCheckInAt(
                OffsetDateTime.ofInstant(
                        NOW.minusSeconds(8 * 60 * 60),
                        ZoneOffset.UTC
                )
        );
        attendance.setOrigin(AttendanceOrigin.TERMINAL);
        attendance.setStatus(AttendanceStatus.OPEN);

        when(attendanceRepository
                .findFirstByBusiness_IdAndEmployee_IdAndStatusOrderByCheckInAtDesc(
                        BUSINESS_ID,
                        EMPLOYEE_ID,
                        AttendanceStatus.OPEN
                )).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(attendance))
                .thenReturn(attendance);

        AttendanceResponse response = attendanceService.checkOut(
                BUSINESS_ID,
                new TerminalAttendanceRequest(EMPLOYEE_ID)
        );

        assertThat(response.status()).isEqualTo(AttendanceStatus.CLOSED);
        assertThat(response.workedMinutes()).isEqualTo(480L);
        assertThat(response.checkOutAt())
                .isEqualTo(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
    }
}
