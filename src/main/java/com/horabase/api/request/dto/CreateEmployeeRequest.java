package com.horabase.api.request.dto;
import com.horabase.api.request.EmployeeRequestType; import jakarta.validation.constraints.*; import java.time.LocalDate;
public record CreateEmployeeRequest(@NotNull EmployeeRequestType type,@NotBlank @Size(max=150)String title,@NotBlank @Size(max=2000)String description,@Positive Long shiftId,LocalDate relatedDate){}
