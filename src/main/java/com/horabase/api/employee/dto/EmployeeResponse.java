package com.horabase.api.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EmployeeResponse(
        Long id,
        Long businessId,
        Long accountId,
        Long sectorId,
        String sectorName,
        String firstName,
        String lastName,
        String document,
        String email,
        String phone,
        LocalDate hireDate,
        BigDecimal hourlyRate,
        BigDecimal overtimeHourlyRate,
        BigDecimal weeklyHours,
        boolean active,
        boolean mustChangePassword,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}