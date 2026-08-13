package com.horabase.api.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveEmployeeRequest(
        @NotBlank @Size(max = 2000) String response
) {
}
