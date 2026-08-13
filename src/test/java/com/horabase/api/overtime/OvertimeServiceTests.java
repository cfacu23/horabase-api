package com.horabase.api.overtime;

import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.attendance.AttendanceRepository;
import com.horabase.api.overtime.dto.ApproveOvertimeRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OvertimeServiceTests {
    @Mock OvertimeRepository repository;
    @Mock BusinessRepository businessRepository;
    @Mock EmployeeRepository employeeRepository;
    @Mock AttendanceRepository attendanceRepository;

    @Test
    void approvalSnapshotsRateAndCalculatesAmount() {
        Business business = new Business(); ReflectionTestUtils.setField(business, "id", 1L);
        Employee employee = new Employee(); ReflectionTestUtils.setField(employee, "id", 2L);
        employee.setFirstName("Ana"); employee.setLastName("Pérez");
        employee.setOvertimeHourlyRate(new BigDecimal("600.00"));
        OvertimeRecord record = new OvertimeRecord(); ReflectionTestUtils.setField(record, "id", 3L);
        record.setBusiness(business); record.setEmployee(employee);
        record.setDetectedMinutes(90); record.setStatus(OvertimeStatus.PENDING);
        when(repository.findByIdAndBusiness_Id(3L, 1L)).thenReturn(Optional.of(record));
        when(repository.save(record)).thenReturn(record);
        OvertimeService service = new OvertimeService(repository, businessRepository,
                employeeRepository, attendanceRepository, Clock.systemUTC());

        var response = service.approve(1L, 3L, new ApproveOvertimeRequest(45, null));

        assertThat(response.hourlyRateSnapshot()).isEqualByComparingTo("600.00");
        assertThat(response.approvedAmount()).isEqualByComparingTo("450.00");
        assertThat(response.status()).isEqualTo(OvertimeStatus.APPROVED);
    }
}
