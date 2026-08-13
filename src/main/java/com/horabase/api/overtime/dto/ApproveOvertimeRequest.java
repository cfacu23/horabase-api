package com.horabase.api.overtime.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ApproveOvertimeRequest(
        @Positive long approvedMinutes,
        @Size(max = 500) String notes
) {}
