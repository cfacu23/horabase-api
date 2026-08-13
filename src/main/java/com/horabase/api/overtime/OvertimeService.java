package com.horabase.api.overtime;

import com.horabase.api.attendance.Attendance;
import com.horabase.api.attendance.AttendanceRepository;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.overtime.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OvertimeService {
    private final OvertimeRepository repository;
    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final Clock clock;

    public OvertimeService(OvertimeRepository repository, BusinessRepository businessRepository,
                           EmployeeRepository employeeRepository, AttendanceRepository attendanceRepository,
                           Clock clock) {
        this.repository = repository;
        this.businessRepository = businessRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.clock = clock;
    }

    @Transactional
    public OvertimeResponse create(Long businessId, CreateOvertimeRequest request) {
        Business business = findBusiness(businessId);
        Employee employee = findEmployee(businessId, request.employeeId());
        Attendance attendance = null;
        if (request.attendanceId() != null) {
            attendance = attendanceRepository.findDetailedByIdAndBusinessId(request.attendanceId(), businessId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la asistencia"));
            if (!attendance.getEmployee().getId().equals(employee.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La asistencia no pertenece al empleado");
            }
        }
        OvertimeRecord record = new OvertimeRecord();
        record.setBusiness(business);
        record.setEmployee(employee);
        record.setAttendance(attendance);
        record.setWorkDate(request.workDate());
        record.setDetectedMinutes(request.detectedMinutes());
        record.setStatus(OvertimeStatus.PENDING);
        record.setNotes(normalize(request.notes()));
        return response(repository.save(record));
    }

    @Transactional(readOnly = true)
    public List<OvertimeResponse> list(Long businessId, LocalDate from, LocalDate to, Long employeeId) {
        findBusiness(businessId);
        if (to.isBefore(from)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El período no es válido");
        List<OvertimeRecord> records = employeeId == null
                ? repository.findAllByBusiness_IdAndWorkDateBetweenOrderByWorkDateDesc(businessId, from, to)
                : repository.findAllByBusiness_IdAndEmployee_IdAndWorkDateBetweenOrderByWorkDateDesc(
                        businessId, findEmployee(businessId, employeeId).getId(), from, to);
        return records.stream().map(this::response).toList();
    }

    @Transactional
    public OvertimeResponse approve(Long businessId, Long id, ApproveOvertimeRequest request) {
        OvertimeRecord record = findRecord(businessId, id);
        if (record.getStatus() != OvertimeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden aprobar horas extra pendientes");
        }
        if (request.approvedMinutes() > record.getDetectedMinutes()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los minutos aprobados no pueden superar los detectados");
        }
        BigDecimal rate = record.getEmployee().getOvertimeHourlyRate().setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = rate.multiply(BigDecimal.valueOf(request.approvedMinutes()))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        record.setApprovedMinutes(request.approvedMinutes());
        record.setHourlyRateSnapshot(rate);
        record.setApprovedAmount(amount);
        record.setStatus(OvertimeStatus.APPROVED);
        appendNote(record, request.notes());
        return response(repository.save(record));
    }

    @Transactional
    public OvertimeResponse reject(Long businessId, Long id, String notes) {
        OvertimeRecord record = findRecord(businessId, id);
        if (record.getStatus() != OvertimeStatus.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden rechazar horas extra pendientes");
        record.setStatus(OvertimeStatus.REJECTED);
        appendNote(record, notes);
        return response(repository.save(record));
    }

    @Transactional
    public OvertimeResponse pay(Long businessId, Long id, PayOvertimeRequest request) {
        OvertimeRecord record = findRecord(businessId, id);
        if (record.getStatus() != OvertimeStatus.APPROVED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden pagar horas extra aprobadas");
        record.setPaidAmount(request.paidAmount().setScale(2, RoundingMode.HALF_UP));
        record.setPaidAt(request.paidAt() == null ? OffsetDateTime.now(clock) : request.paidAt());
        record.setStatus(OvertimeStatus.PAID);
        appendNote(record, request.notes());
        return response(repository.save(record));
    }

    private Business findBusiness(Long id) { return businessRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el comercio")); }
    private Employee findEmployee(Long businessId, Long id) { return employeeRepository.findByIdAndBusiness_Id(id, businessId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el empleado")); }
    private OvertimeRecord findRecord(Long businessId, Long id) { return repository.findByIdAndBusiness_Id(id, businessId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el registro de horas extra")); }
    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private void appendNote(OvertimeRecord record, String note) { String n = normalize(note); if (n != null) record.setNotes(record.getNotes() == null ? n : record.getNotes() + System.lineSeparator() + n); }
    private OvertimeResponse response(OvertimeRecord r) { Employee e = r.getEmployee(); return new OvertimeResponse(r.getId(), r.getBusiness().getId(), e.getId(), e.getFirstName()+" "+e.getLastName(), r.getAttendance()==null?null:r.getAttendance().getId(), r.getWorkDate(), r.getDetectedMinutes(), r.getApprovedMinutes(), r.getHourlyRateSnapshot(), r.getApprovedAmount(), r.getStatus(), r.getPaidAt(), r.getPaidAmount(), r.getNotes(), r.getCreatedAt(), r.getUpdatedAt()); }
}
