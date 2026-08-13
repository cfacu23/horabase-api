package com.horabase.api.employee;

import com.horabase.api.attendance.AttendanceService;
import com.horabase.api.employee.dto.EmployeeProfileResponse;
import com.horabase.api.incident.AttendanceIncidentService;
import com.horabase.api.overtime.OvertimeService;
import com.horabase.api.request.EmployeeRequestService;
import com.horabase.api.shift.ShiftService;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class EmployeeProfileService {

    private final EmployeeService employees;
    private final ShiftService shifts;
    private final AttendanceService attendances;
    private final AttendanceIncidentService incidents;
    private final OvertimeService overtime;
    private final EmployeeRequestService requests;
    private final Clock clock;

    public EmployeeProfileService(
            EmployeeService employees,
            ShiftService shifts,
            AttendanceService attendances,
            AttendanceIncidentService incidents,
            OvertimeService overtime,
            EmployeeRequestService requests,
            Clock clock
    ) {
        this.employees = employees;
        this.shifts = shifts;
        this.attendances = attendances;
        this.incidents = incidents;
        this.overtime = overtime;
        this.requests = requests;
        this.clock = clock;
    }

    public EmployeeProfileResponse get(Long businessId, Long employeeId) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        LocalDate today = LocalDate.now(clock);

        return new EmployeeProfileResponse(
                employees.findById(businessId, employeeId),
                shifts.findAllByPeriod(
                        businessId, now, now.plusDays(30), employeeId
                ),
                attendances.findAllByPeriod(
                        businessId, now.minusDays(30), now, employeeId, null
                ),
                incidents.list(
                        businessId, today.minusDays(90), today, employeeId
                ),
                overtime.list(
                        businessId, today.minusDays(90), today, employeeId
                ),
                requests.listForEmployee(businessId, employeeId)
        );
    }
}
