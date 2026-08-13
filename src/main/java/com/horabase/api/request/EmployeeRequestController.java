package com.horabase.api.request;

import com.horabase.api.auth.CurrentUser;
import com.horabase.api.request.dto.CreateEmployeeRequest;
import com.horabase.api.request.dto.EmployeeRequestResponse;
import com.horabase.api.request.dto.ResolveEmployeeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EmployeeRequestController {

    private final EmployeeRequestService service;

    public EmployeeRequestController(EmployeeRequestService service) {
        this.service = service;
    }

    @PostMapping("/api/me/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeRequestResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return service.create(CurrentUser.from(jwt), request);
    }

    @GetMapping("/api/me/requests")
    public List<EmployeeRequestResponse> mine(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.mine(CurrentUser.from(jwt));
    }

    @PostMapping("/api/me/requests/{requestId}/cancel")
    public EmployeeRequestResponse cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long requestId
    ) {
        return service.cancel(CurrentUser.from(jwt), requestId);
    }

    @GetMapping("/api/businesses/{businessId}/requests")
    @PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
    public List<EmployeeRequestResponse> list(
            @PathVariable Long businessId,
            @RequestParam(required = false) EmployeeRequestStatus status
    ) {
        return service.list(businessId, status);
    }

    @PostMapping("/api/businesses/{businessId}/requests/{requestId}/approve")
    @PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
    public EmployeeRequestResponse approve(
            @PathVariable Long businessId,
            @PathVariable Long requestId,
            @Valid @RequestBody ResolveEmployeeRequest request
    ) {
        return service.resolve(
                businessId,
                requestId,
                EmployeeRequestStatus.APPROVED,
                request
        );
    }

    @PostMapping("/api/businesses/{businessId}/requests/{requestId}/reject")
    @PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
    public EmployeeRequestResponse reject(
            @PathVariable Long businessId,
            @PathVariable Long requestId,
            @Valid @RequestBody ResolveEmployeeRequest request
    ) {
        return service.resolve(
                businessId,
                requestId,
                EmployeeRequestStatus.REJECTED,
                request
        );
    }
}
