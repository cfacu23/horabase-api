package com.horabase.api.employee.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEmployeeRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 80)
        String lastName,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phone,

        @NotNull(message = "El sector es obligatorio")
        @Positive(message = "El identificador del sector debe ser válido")
        Long sectorId,

        @NotNull(message = "La fecha de ingreso es obligatoria")
        @PastOrPresent(message = "La fecha de ingreso no puede estar en el futuro")
        LocalDate hireDate,

        @NotNull(message = "El valor de la hora es obligatorio")
        @DecimalMin(value = "0.00", message = "El valor no puede ser negativo")
        @Digits(integer = 10, fraction = 2)
        BigDecimal hourlyRate,

        @NotNull(message = "El valor de la hora extra es obligatorio")
        @DecimalMin(value = "0.00", message = "El valor no puede ser negativo")
        @Digits(integer = 10, fraction = 2)
        BigDecimal overtimeHourlyRate,

        @NotNull(message = "La carga horaria semanal es obligatoria")
        @DecimalMin(value = "0.01")
        @DecimalMax(value = "168.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal weeklyHours,

        boolean active
) {
}