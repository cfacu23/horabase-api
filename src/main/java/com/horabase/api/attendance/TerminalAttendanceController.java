package com.horabase.api.attendance;

import com.horabase.api.attendance.dto.AttendanceResponse;
import com.horabase.api.attendance.dto.TerminalAttendanceRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/terminal/businesses/{businessId}")
@Tag(
        name = "Terminal de asistencia",
        description = "Marcaciones realizadas desde un dispositivo del comercio"
)
public class TerminalAttendanceController {

    private final AttendanceService attendanceService;

    public TerminalAttendanceController(
            AttendanceService attendanceService
    ) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public AttendanceResponse checkIn(
            @PathVariable Long businessId,
            @Valid @RequestBody TerminalAttendanceRequest request
    ) {
        return attendanceService.checkIn(businessId, request);
    }

    @PostMapping("/check-out")
    public AttendanceResponse checkOut(
            @PathVariable Long businessId,
            @Valid @RequestBody TerminalAttendanceRequest request
    ) {
        return attendanceService.checkOut(businessId, request);
    }
}
