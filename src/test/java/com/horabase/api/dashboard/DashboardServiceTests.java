package com.horabase.api.dashboard;

import com.horabase.api.attendance.AttendanceRepository;
import com.horabase.api.attendance.AttendanceStatus;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.incident.AttendanceIncidentRepository;
import com.horabase.api.incident.IncidentStatus;
import com.horabase.api.incident.IncidentType;
import com.horabase.api.overtime.OvertimeRepository;
import com.horabase.api.overtime.OvertimeStatus;
import com.horabase.api.request.EmployeeRequestRepository;
import com.horabase.api.request.EmployeeRequestStatus;
import com.horabase.api.shift.ShiftRepository;
import com.horabase.api.shift.ShiftStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTests {

    @Mock BusinessRepository businesses;
    @Mock EmployeeRepository employees;
    @Mock AttendanceRepository attendances;
    @Mock ShiftRepository shifts;
    @Mock AttendanceIncidentRepository incidents;
    @Mock EmployeeRequestRepository requests;
    @Mock OvertimeRepository overtime;

    @Test
    void returnsAggregateSummaryWithoutLoadingCollections() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-13T12:00:00Z"), ZoneOffset.UTC
        );
        LocalDate today = LocalDate.of(2026, 8, 13);
        OffsetDateTime now = OffsetDateTime.now(clock);

        when(businesses.existsById(1L)).thenReturn(true);
        when(employees.countByBusiness_IdAndActiveTrue(1L)).thenReturn(12L);
        when(attendances.countByBusiness_IdAndStatus(1L, AttendanceStatus.OPEN))
                .thenReturn(4L);
        when(shifts.countByBusiness_IdAndStatusAndStartsAtBetween(
                1L, ShiftStatus.SCHEDULED, now, now.plusDays(7)
        )).thenReturn(18L);
        when(incidents.countByBusiness_IdAndIncidentDateAndTypeAndStatusNot(
                1L, today, IncidentType.LATE_ARRIVAL, IncidentStatus.DISMISSED
        )).thenReturn(2L);
        when(incidents.countByBusiness_IdAndIncidentDateAndTypeAndStatusNot(
                1L, today, IncidentType.ABSENCE, IncidentStatus.DISMISSED
        )).thenReturn(1L);
        when(requests.countByBusiness_IdAndStatus(
                1L, EmployeeRequestStatus.PENDING
        )).thenReturn(3L);
        when(overtime.countByBusiness_IdAndStatus(1L, OvertimeStatus.PENDING))
                .thenReturn(5L);

        DashboardService service = new DashboardService(
                businesses, employees, attendances, shifts,
                incidents, requests, overtime, clock
        );

        AdminDashboardResponse result = service.summary(1L);

        assertThat(result.activeEmployees()).isEqualTo(12);
        assertThat(result.workingNow()).isEqualTo(4);
        assertThat(result.upcomingShifts()).isEqualTo(18);
        assertThat(result.pendingOvertime()).isEqualTo(5);
    }
}
