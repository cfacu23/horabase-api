package com.horabase.api.request.dto;

import com.horabase.api.request.EmployeeRequestStatus;
import com.horabase.api.request.EmployeeRequestType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EmployeeRequestResponse(
        Long id,
        Long businessId,
        Long employeeId,
        String employeeName,
        EmployeeRequestType type,
        String title,
        String description,
        Long shiftId,
        LocalDate relatedDate,
        EmployeeRequestStatus status,
        String adminResponse,
        OffsetDateTime resolvedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
