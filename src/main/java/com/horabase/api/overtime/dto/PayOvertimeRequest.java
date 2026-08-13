package com.horabase.api.overtime.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PayOvertimeRequest(
        @NotNull @DecimalMin("0.00") @Digits(integer = 12, fraction = 2)
        BigDecimal paidAmount,
        OffsetDateTime paidAt,
        @Size(max = 500) String notes
) {}
