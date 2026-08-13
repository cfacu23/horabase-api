package com.horabase.api.incident;

import com.horabase.api.incident.dto.IncidentRequest;
import com.horabase.api.incident.dto.IncidentResponse;
import com.horabase.api.incident.dto.ResolveIncidentRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/incidents")
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class AttendanceIncidentController {

    private final AttendanceIncidentService service;

    public AttendanceIncidentController(AttendanceIncidentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(
            @PathVariable Long businessId,
            @Valid @RequestBody IncidentRequest request
    ) {
        return service.create(businessId, request);
    }

    @GetMapping
    public List<IncidentResponse> list(
            @PathVariable Long businessId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false) Long employeeId
    ) {
        return service.list(businessId, from, to, employeeId);
    }

    @PostMapping("/{incidentId}/resolve")
    public IncidentResponse resolve(
            @PathVariable Long businessId,
            @PathVariable Long incidentId,
            @Valid @RequestBody ResolveIncidentRequest request
    ) {
        return service.resolve(businessId, incidentId, request);
    }
}
