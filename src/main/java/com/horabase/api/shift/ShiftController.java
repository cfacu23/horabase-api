package com.horabase.api.shift;

import com.horabase.api.shift.dto.CreateShiftRequest;
import com.horabase.api.shift.dto.ShiftResponse;
import com.horabase.api.shift.dto.UpdateShiftRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/shifts")
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse create(
            @PathVariable Long businessId,
            @Valid @RequestBody CreateShiftRequest request
    ) {
        return shiftService.create(businessId, request);
    }

    @GetMapping
    public List<ShiftResponse> findAll(
            @PathVariable Long businessId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,

            @RequestParam(required = false)
            Long employeeId,

            @RequestParam(required = false)
            Long sectorId
    ) {
        return shiftService.findAllByPeriod(
                businessId,
                from,
                to,
                employeeId,
                sectorId
        );
    }

    @GetMapping("/{shiftId}")
    public ShiftResponse findById(
            @PathVariable Long businessId,
            @PathVariable Long shiftId
    ) {
        return shiftService.findById(businessId, shiftId);
    }

    @PutMapping("/{shiftId}")
    public ShiftResponse update(
            @PathVariable Long businessId,
            @PathVariable Long shiftId,
            @Valid @RequestBody UpdateShiftRequest request
    ) {
        return shiftService.update(
                businessId,
                shiftId,
                request
        );
    }
}
