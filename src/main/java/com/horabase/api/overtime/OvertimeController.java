package com.horabase.api.overtime;

import com.horabase.api.overtime.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/overtime")
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class OvertimeController {
    private final OvertimeService service;
    public OvertimeController(OvertimeService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public OvertimeResponse create(@PathVariable Long businessId, @Valid @RequestBody CreateOvertimeRequest request) { return service.create(businessId, request); }
    @GetMapping
    public List<OvertimeResponse> list(@PathVariable Long businessId, @RequestParam LocalDate from, @RequestParam LocalDate to, @RequestParam(required=false) Long employeeId) { return service.list(businessId, from, to, employeeId); }
    @PostMapping("/{id}/approve")
    public OvertimeResponse approve(@PathVariable Long businessId, @PathVariable Long id, @Valid @RequestBody ApproveOvertimeRequest request) { return service.approve(businessId, id, request); }
    @PostMapping("/{id}/reject")
    public OvertimeResponse reject(@PathVariable Long businessId, @PathVariable Long id, @RequestBody(required=false) String notes) { return service.reject(businessId, id, notes); }
    @PostMapping("/{id}/pay")
    public OvertimeResponse pay(@PathVariable Long businessId, @PathVariable Long id, @Valid @RequestBody PayOvertimeRequest request) { return service.pay(businessId, id, request); }
}
