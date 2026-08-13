package com.horabase.api.attendance;

import com.horabase.api.attendance.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/attendances")
@Tag(
        name = "Asistencias",
        description = "Consulta, alta manual, corrección y anulación de asistencias"
)
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<AttendanceResponse> findAll(
            @PathVariable Long businessId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) AttendanceStatus status
    ) {
        return attendanceService.findAllByPeriod(
                businessId,
                from,
                to,
                employeeId,
                status
        );
    }

    @GetMapping("/{attendanceId}")
    public AttendanceResponse findById(
            @PathVariable Long businessId,
            @PathVariable Long attendanceId
    ) {
        return attendanceService.findById(businessId, attendanceId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse createManual(
            @PathVariable Long businessId,
            @Valid @RequestBody CreateManualAttendanceRequest request
    ) {
        return attendanceService.createManual(businessId, request);
    }

    @PatchMapping("/{attendanceId}/check-in")
    public AttendanceResponse correctCheckIn(
            @PathVariable Long businessId,
            @PathVariable Long attendanceId,
            @Valid @RequestBody CorrectAttendanceTimeRequest request
    ) {
        return attendanceService.correctCheckIn(
                businessId,
                attendanceId,
                request
        );
    }

    @PatchMapping("/{attendanceId}/check-out")
    public AttendanceResponse correctCheckOut(
            @PathVariable Long businessId,
            @PathVariable Long attendanceId,
            @Valid @RequestBody CorrectAttendanceTimeRequest request
    ) {
        return attendanceService.correctCheckOut(
                businessId,
                attendanceId,
                request
        );
    }

    @PostMapping("/{attendanceId}/cancel")
    public AttendanceResponse cancel(
            @PathVariable Long businessId,
            @PathVariable Long attendanceId,
            @Valid @RequestBody CancelAttendanceRequest request
    ) {
        return attendanceService.cancel(
                businessId,
                attendanceId,
                request
        );
    }
}
