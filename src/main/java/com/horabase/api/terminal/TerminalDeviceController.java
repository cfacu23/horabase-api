package com.horabase.api.terminal;

import com.horabase.api.terminal.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/terminals")
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class TerminalDeviceController {

    private final TerminalDeviceService terminalService;

    public TerminalDeviceController(TerminalDeviceService terminalService) {
        this.terminalService = terminalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TerminalDeviceCredentialsResponse create(
            @PathVariable Long businessId,
            @Valid @RequestBody CreateTerminalDeviceRequest request
    ) {
        return terminalService.create(businessId, request);
    }

    @GetMapping
    public List<TerminalDeviceResponse> findAll(
            @PathVariable Long businessId
    ) {
        return terminalService.findAll(businessId);
    }

    @PutMapping("/{terminalId}")
    public TerminalDeviceResponse update(
            @PathVariable Long businessId,
            @PathVariable Long terminalId,
            @Valid @RequestBody UpdateTerminalDeviceRequest request
    ) {
        return terminalService.update(businessId, terminalId, request);
    }

    @PostMapping("/{terminalId}/rotate-secret")
    public TerminalDeviceCredentialsResponse rotateSecret(
            @PathVariable Long businessId,
            @PathVariable Long terminalId
    ) {
        return terminalService.rotateSecret(businessId, terminalId);
    }
}
