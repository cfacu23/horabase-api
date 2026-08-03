package com.horabase.api.employee.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEmployeeRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 80, message = "El apellido no puede superar los 80 caracteres")
        String lastName,

        @NotBlank(message = "La cédula es obligatoria")
        @Size(max = 20, message = "La cédula no puede superar los 20 caracteres")
        String document,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String email,

        @NotBlank(message = "La contraseña temporal es obligatoria")
        @Size(
                min = 8,
                max = 72,
                message = "La contraseña debe tener entre 8 y 72 caracteres"
        )
        String temporaryPassword,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String phone,

        @NotNull(message = "El sector es obligatorio")
        @Positive(message = "El identificador del sector debe ser válido")
        Long sectorId,

        @NotNull(message = "La fecha de ingreso es obligatoria")
        @PastOrPresent(message = "La fecha de ingreso no puede estar en el futuro")
        LocalDate hireDate,

        @NotNull(message = "El valor de la hora es obligatorio")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "El valor de la hora no puede ser negativo"
        )
        @Digits(integer = 10, fraction = 2)
        BigDecimal hourlyRate,

        @NotNull(message = "El valor de la hora extra es obligatorio")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "El valor de la hora extra no puede ser negativo"
        )
        @Digits(integer = 10, fraction = 2)
        BigDecimal overtimeHourlyRate,

        @NotNull(message = "La carga horaria semanal es obligatoria")
        @DecimalMin(
                value = "0.01",
                message = "La carga horaria semanal debe ser mayor que cero"
        )
        @DecimalMax(
                value = "168.00",
                message = "La carga horaria semanal no puede superar las 168 horas"
        )
        @Digits(integer = 3, fraction = 2)
        BigDecimal weeklyHours
) {
}