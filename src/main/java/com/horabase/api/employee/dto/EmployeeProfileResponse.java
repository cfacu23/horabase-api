package com.horabase.api.employee.dto;

import com.horabase.api.attendance.dto.AttendanceResponse;
import com.horabase.api.incident.dto.IncidentResponse;
import com.horabase.api.overtime.dto.OvertimeResponse;
import com.horabase.api.request.dto.EmployeeRequestResponse;
import com.horabase.api.shift.dto.ShiftResponse;

import java.util.List;

public record EmployeeProfileResponse(
        EmployeeResponse employee,
        List<ShiftResponse> upcomingShifts,
        List<AttendanceResponse> recentAttendances,
        List<IncidentResponse> recentIncidents,
        List<OvertimeResponse> recentOvertime,
        List<EmployeeRequestResponse> requests
) {
}
