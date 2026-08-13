package com.horabase.api.attendance;

import com.horabase.api.attendance.dto.*;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.shift.Shift;
import com.horabase.api.shift.ShiftRepository;
import com.horabase.api.shift.ShiftStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AttendanceService {

    private static final int MAX_NOTES_LENGTH = 1000;

    private final AttendanceRepository attendanceRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final Clock clock;
    private final long shiftMatchWindowMinutes;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            BusinessRepository businessRepository,
            EmployeeRepository employeeRepository,
            ShiftRepository shiftRepository,
            Clock clock,
            @Value("${attendance.shift-match-window-minutes:240}")
            long shiftMatchWindowMinutes
    ) {
        this.attendanceRepository = attendanceRepository;
        this.businessRepository = businessRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.clock = clock;
        this.shiftMatchWindowMinutes = shiftMatchWindowMinutes;
    }

    @Transactional
    public AttendanceResponse checkIn(
            Long businessId,
            TerminalAttendanceRequest request
    ) {
        Business business = findBusinessById(businessId);
        Employee employee = findEmployeeForUpdate(
                businessId,
                request.employeeId()
        );

        validateCanClock(business, employee);
        ensureNoOpenAttendance(businessId, employee.getId());

        OffsetDateTime now = OffsetDateTime.now(clock);
        Shift shift = findClosestShift(
                businessId,
                employee.getId(),
                now
        );

        Attendance attendance = new Attendance();
        attendance.setBusiness(business);
        attendance.setEmployee(employee);
        attendance.setShift(shift);
        attendance.setCheckInAt(now);
        attendance.setOrigin(AttendanceOrigin.TERMINAL);
        attendance.setStatus(AttendanceStatus.OPEN);

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse checkOut(
            Long businessId,
            TerminalAttendanceRequest request
    ) {
        Business business = findBusinessById(businessId);
        Employee employee = findEmployeeForUpdate(
                businessId,
                request.employeeId()
        );

        validateCanClock(business, employee);

        Attendance attendance = attendanceRepository
                .findFirstByBusiness_IdAndEmployee_IdAndStatusOrderByCheckInAtDesc(
                        businessId,
                        employee.getId(),
                        AttendanceStatus.OPEN
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El empleado no tiene una asistencia abierta"
                ));

        OffsetDateTime now = OffsetDateTime.now(clock);

        if (!now.isAfter(attendance.getCheckInAt())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La salida debe ser posterior a la entrada"
            );
        }

        close(attendance, now);

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse createManual(
            Long businessId,
            CreateManualAttendanceRequest request
    ) {
        Business business = findBusinessById(businessId);
        Employee employee = findEmployeeForUpdate(
                businessId,
                request.employeeId()
        );

        if (request.checkOutAt() != null
                && !request.checkOutAt().isAfter(request.checkInAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La salida debe ser posterior a la entrada"
            );
        }

        if (request.checkOutAt() == null) {
            validateCanClock(business, employee);
            ensureNoOpenAttendance(businessId, employee.getId());
        }

        Shift shift = request.shiftId() == null
                ? findClosestShift(
                        businessId,
                        employee.getId(),
                        request.checkInAt()
                )
                : findShiftForEmployee(
                        businessId,
                        employee.getId(),
                        request.shiftId()
                );

        Attendance attendance = new Attendance();
        attendance.setBusiness(business);
        attendance.setEmployee(employee);
        attendance.setShift(shift);
        attendance.setCheckInAt(request.checkInAt());
        attendance.setOrigin(AttendanceOrigin.MANUAL);
        attendance.setNotes(normalizeNullable(request.notes()));

        if (request.checkOutAt() == null) {
            attendance.setStatus(AttendanceStatus.OPEN);
        } else {
            close(attendance, request.checkOutAt());
        }

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> findAllByPeriod(
            Long businessId,
            OffsetDateTime from,
            OffsetDateTime to,
            Long employeeId,
            AttendanceStatus status
    ) {
        findBusinessById(businessId);
        validateSearchPeriod(from, to);

        if (employeeId != null) {
            findEmployeeById(businessId, employeeId);
        }

        return attendanceRepository
                .findAllByPeriod(
                        businessId,
                        from,
                        to,
                        employeeId,
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceResponse findById(
            Long businessId,
            Long attendanceId
    ) {
        return toResponse(findAttendanceById(businessId, attendanceId));
    }

    @Transactional
    public AttendanceResponse correctCheckIn(
            Long businessId,
            Long attendanceId,
            CorrectAttendanceTimeRequest request
    ) {
        Attendance attendance = findAttendanceById(
                businessId,
                attendanceId
        );
        validateNotCancelled(attendance);

        if (attendance.getCheckOutAt() != null
                && !attendance.getCheckOutAt().isAfter(request.occurredAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La entrada debe ser anterior a la salida"
            );
        }

        OffsetDateTime previous = attendance.getCheckInAt();
        attendance.setCheckInAt(request.occurredAt());
        recalculateWorkedMinutes(attendance);
        recordCorrection(
                attendance,
                "entrada",
                previous,
                request.occurredAt(),
                request.reason()
        );

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse correctCheckOut(
            Long businessId,
            Long attendanceId,
            CorrectAttendanceTimeRequest request
    ) {
        Attendance attendance = findAttendanceById(
                businessId,
                attendanceId
        );
        validateNotCancelled(attendance);

        if (!request.occurredAt().isAfter(attendance.getCheckInAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La salida debe ser posterior a la entrada"
            );
        }

        OffsetDateTime previous = attendance.getCheckOutAt();
        close(attendance, request.occurredAt());
        recordCorrection(
                attendance,
                "salida",
                previous,
                request.occurredAt(),
                request.reason()
        );

        return toResponse(attendanceRepository.save(attendance));
    }

    @Transactional
    public AttendanceResponse cancel(
            Long businessId,
            Long attendanceId,
            CancelAttendanceRequest request
    ) {
        Attendance attendance = findAttendanceById(
                businessId,
                attendanceId
        );
        validateNotCancelled(attendance);

        attendance.setStatus(AttendanceStatus.CANCELLED);
        appendNote(
                attendance,
                "Anulada: " + request.reason().trim()
        );

        return toResponse(attendanceRepository.save(attendance));
    }

    private void close(Attendance attendance, OffsetDateTime checkOutAt) {
        attendance.setCheckOutAt(checkOutAt);
        attendance.setWorkedMinutes(
                Duration.between(
                        attendance.getCheckInAt(),
                        checkOutAt
                ).toMinutes()
        );
        attendance.setStatus(AttendanceStatus.CLOSED);
    }

    private void recalculateWorkedMinutes(Attendance attendance) {
        if (attendance.getCheckOutAt() != null) {
            attendance.setWorkedMinutes(
                    Duration.between(
                            attendance.getCheckInAt(),
                            attendance.getCheckOutAt()
                    ).toMinutes()
            );
        }
    }

    private Shift findClosestShift(
            Long businessId,
            Long employeeId,
            OffsetDateTime checkInAt
    ) {
        OffsetDateTime from = checkInAt.minusMinutes(
                shiftMatchWindowMinutes
        );
        OffsetDateTime to = checkInAt.plusMinutes(
                shiftMatchWindowMinutes
        );

        return shiftRepository
                .findAllByBusiness_IdAndEmployee_IdAndStatusAndStartsAtBetweenOrderByStartsAtAsc(
                        businessId,
                        employeeId,
                        ShiftStatus.SCHEDULED,
                        from,
                        to
                )
                .stream()
                .min(Comparator.comparingLong(shift ->
                        Math.abs(Duration.between(
                                checkInAt,
                                shift.getStartsAt()
                        ).toMinutes())
                ))
                .orElse(null);
    }

    private Shift findShiftForEmployee(
            Long businessId,
            Long employeeId,
            Long shiftId
    ) {
        Shift shift = shiftRepository
                .findByIdAndBusiness_Id(shiftId, businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el turno"
                ));

        if (!shift.getEmployee().getId().equals(employeeId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El turno no pertenece al empleado"
            );
        }

        return shift;
    }

    private void ensureNoOpenAttendance(
            Long businessId,
            Long employeeId
    ) {
        if (attendanceRepository
                .existsByBusiness_IdAndEmployee_IdAndStatus(
                        businessId,
                        employeeId,
                        AttendanceStatus.OPEN
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El empleado ya tiene una asistencia abierta"
            );
        }
    }

    private void validateCanClock(
            Business business,
            Employee employee
    ) {
        if (!business.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El comercio está inactivo"
            );
        }

        if (!employee.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El empleado está inactivo"
            );
        }

        if (!employee.getAccount().isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cuenta del empleado está inactiva"
            );
        }
    }

    private void validateSearchPeriod(
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        if (!to.isAfter(from)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El final del período debe ser posterior al inicio"
            );
        }
    }

    private void validateNotCancelled(Attendance attendance) {
        if (attendance.getStatus() == AttendanceStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La asistencia está anulada"
            );
        }
    }

    private Business findBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el comercio"
                ));
    }

    private Employee findEmployeeById(
            Long businessId,
            Long employeeId
    ) {
        return employeeRepository
                .findByIdAndBusiness_Id(employeeId, businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el empleado"
                ));
    }

    private Employee findEmployeeForUpdate(
            Long businessId,
            Long employeeId
    ) {
        return employeeRepository
                .findByIdAndBusinessIdForUpdate(
                        employeeId,
                        businessId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el empleado"
                ));
    }

    private Attendance findAttendanceById(
            Long businessId,
            Long attendanceId
    ) {
        return attendanceRepository
                .findDetailedByIdAndBusinessId(
                        attendanceId,
                        businessId
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la asistencia"
                ));
    }

    private void recordCorrection(
            Attendance attendance,
            String field,
            OffsetDateTime previous,
            OffsetDateTime current,
            String reason
    ) {
        appendNote(
                attendance,
                "Corrección de " + field
                        + " (" + previous + " -> " + current + "): "
                        + reason.trim()
        );
    }

    private void appendNote(Attendance attendance, String entry) {
        String timestampedEntry = "[" + OffsetDateTime.now(clock)
                + "] " + entry;
        String existing = normalizeNullable(attendance.getNotes());
        String notes = existing == null
                ? timestampedEntry
                : existing + System.lineSeparator() + timestampedEntry;

        if (notes.length() > MAX_NOTES_LENGTH) {
            notes = notes.substring(notes.length() - MAX_NOTES_LENGTH);
        }

        attendance.setNotes(notes);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        Employee employee = attendance.getEmployee();
        Shift shift = attendance.getShift();

        return new AttendanceResponse(
                attendance.getId(),
                attendance.getBusiness().getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                shift == null ? null : shift.getId(),
                shift == null ? null : shift.getStartsAt(),
                shift == null ? null : shift.getEndsAt(),
                attendance.getCheckInAt(),
                attendance.getCheckOutAt(),
                attendance.getWorkedMinutes(),
                attendance.getOrigin(),
                attendance.getStatus(),
                attendance.getNotes(),
                attendance.getCreatedAt(),
                attendance.getUpdatedAt()
        );
    }
}
