package com.horabase.api.shift.dto;

import com.horabase.api.shift.ShiftStatus;

import java.time.OffsetDateTime;

public record ShiftResponse(
        Long id,
        Long businessId,
        Long employeeId,
        String employeeName,
        Long sectorId,
        String sectorName,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        int breakMinutes,
        long plannedWorkMinutes,
        String notes,
        ShiftStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}