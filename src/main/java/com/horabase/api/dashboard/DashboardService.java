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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
public class DashboardService {

    private final BusinessRepository businesses;
    private final EmployeeRepository employees;
    private final AttendanceRepository attendances;
    private final ShiftRepository shifts;
    private final AttendanceIncidentRepository incidents;
    private final EmployeeRequestRepository requests;
    private final OvertimeRepository overtime;
    private final Clock clock;

    public DashboardService(
            BusinessRepository businesses,
            EmployeeRepository employees,
            AttendanceRepository attendances,
            ShiftRepository shifts,
            AttendanceIncidentRepository incidents,
            EmployeeRequestRepository requests,
            OvertimeRepository overtime,
            Clock clock
    ) {
        this.businesses = businesses;
        this.employees = employees;
        this.attendances = attendances;
        this.shifts = shifts;
        this.incidents = incidents;
        this.requests = requests;
        this.overtime = overtime;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse summary(Long businessId) {
        if (!businesses.existsById(businessId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontr\u00f3 el comercio"
            );
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        LocalDate today = LocalDate.now(clock);

        return new AdminDashboardResponse(
                businessId,
                now,
                today,
                employees.countByBusiness_IdAndActiveTrue(businessId),
                attendances.countByBusiness_IdAndStatus(
                        businessId, AttendanceStatus.OPEN
                ),
                shifts.countByBusiness_IdAndStatusAndStartsAtBetween(
                        businessId,
                        ShiftStatus.SCHEDULED,
                        now,
                        now.plusDays(7)
                ),
                incidents.countByBusiness_IdAndIncidentDateAndTypeAndStatusNot(
                        businessId,
                        today,
                        IncidentType.LATE_ARRIVAL,
                        IncidentStatus.DISMISSED
                ),
                incidents.countByBusiness_IdAndIncidentDateAndTypeAndStatusNot(
                        businessId,
                        today,
                        IncidentType.ABSENCE,
                        IncidentStatus.DISMISSED
                ),
                requests.countByBusiness_IdAndStatus(
                        businessId, EmployeeRequestStatus.PENDING
                ),
                overtime.countByBusiness_IdAndStatus(
                        businessId, OvertimeStatus.PENDING
                )
        );
    }
}
