package com.horabase.api.overtime.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateOvertimeRequest(
        @NotNull @Positive Long employeeId,
        @Positive Long attendanceId,
        @NotNull LocalDate workDate,
        @Positive long detectedMinutes,
        @Size(max = 500) String notes
) {}
