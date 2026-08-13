package com.horabase.api.overtime.dto;

import com.horabase.api.overtime.OvertimeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record OvertimeResponse(
        Long id, Long businessId, Long employeeId, String employeeName,
        Long attendanceId, LocalDate workDate, long detectedMinutes,
        Long approvedMinutes, BigDecimal hourlyRateSnapshot,
        BigDecimal approvedAmount, OvertimeStatus status,
        OffsetDateTime paidAt, BigDecimal paidAmount, String notes,
        OffsetDateTime createdAt, OffsetDateTime updatedAt
) {}
