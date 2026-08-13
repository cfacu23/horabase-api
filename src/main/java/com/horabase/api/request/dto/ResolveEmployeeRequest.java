package com.horabase.api.request.dto;
import jakarta.validation.constraints.*;
public record ResolveEmployeeRequest(@NotBlank @Size(max=2000)String response){}
