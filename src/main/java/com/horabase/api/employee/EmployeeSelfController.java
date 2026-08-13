package com.horabase.api.employee;

import com.horabase.api.attendance.AttendanceService;
import com.horabase.api.attendance.dto.AttendanceResponse;
import com.horabase.api.auth.CurrentUser;
import com.horabase.api.employee.dto.EmployeeProfileResponse;
import com.horabase.api.incident.AttendanceIncidentService;
import com.horabase.api.incident.dto.IncidentResponse;
import com.horabase.api.overtime.OvertimeService;
import com.horabase.api.overtime.dto.OvertimeResponse;
import com.horabase.api.shift.ShiftService;
import com.horabase.api.shift.dto.ShiftResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/me")
public class EmployeeSelfController {

    private final EmployeeProfileService profiles;
    private final ShiftService shifts;
    private final AttendanceService attendances;
    private final AttendanceIncidentService incidents;
    private final OvertimeService overtime;

    public EmployeeSelfController(
            EmployeeProfileService profiles,
            ShiftService shifts,
            AttendanceService attendances,
            AttendanceIncidentService incidents,
            OvertimeService overtime
    ) {
        this.profiles = profiles;
        this.shifts = shifts;
        this.attendances = attendances;
        this.incidents = incidents;
        this.overtime = overtime;
    }

    @GetMapping("/profile")
    public EmployeeProfileResponse profile(@AuthenticationPrincipal Jwt jwt) {
        CurrentUser user = employee(jwt);
        return profiles.get(user.businessId(), user.employeeId());
    }

    @GetMapping("/calendar")
    public List<ShiftResponse> calendar(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to
    ) {
        CurrentUser user = employee(jwt);
        return shifts.findAllByPeriod(
                user.businessId(), from, to, user.employeeId()
        );
    }

    @GetMapping("/attendances")
    public List<AttendanceResponse> attendances(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to
    ) {
        CurrentUser user = employee(jwt);
        return attendances.findAllByPeriod(
                user.businessId(), from, to, user.employeeId(), null
        );
    }

    @GetMapping("/incidents")
    public List<IncidentResponse> incidents(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        CurrentUser user = employee(jwt);
        return incidents.list(
                user.businessId(), from, to, user.employeeId()
        );
    }

    @GetMapping("/overtime")
    public List<OvertimeResponse> overtime(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        CurrentUser user = employee(jwt);
        return overtime.list(
                user.businessId(), from, to, user.employeeId()
        );
    }

    private CurrentUser employee(Jwt jwt) {
        CurrentUser user = CurrentUser.from(jwt);
        if (user.employeeId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La cuenta no pertenece a un empleado"
            );
        }
        return user;
    }
}
