package com.horabase.api.attendance.dto;

import com.horabase.api.attendance.AttendanceOrigin;
import com.horabase.api.attendance.AttendanceStatus;

import java.time.OffsetDateTime;

public record AttendanceResponse(
        Long id,
        Long businessId,
        Long employeeId,
        String employeeName,
        Long shiftId,
        OffsetDateTime scheduledStartsAt,
        OffsetDateTime scheduledEndsAt,
        OffsetDateTime checkInAt,
        OffsetDateTime checkOutAt,
        Long workedMinutes,
        AttendanceOrigin origin,
        AttendanceStatus status,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
