package com.horabase.api.employee;

import com.horabase.api.employee.dto.EmployeeProfileResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businesses/{businessId}/employees/{employeeId}/profile")
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class EmployeeProfileController {

    private final EmployeeProfileService profiles;

    public EmployeeProfileController(EmployeeProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public EmployeeProfileResponse get(
            @PathVariable Long businessId,
            @PathVariable Long employeeId
    ) {
        return profiles.get(businessId, employeeId);
    }
}
