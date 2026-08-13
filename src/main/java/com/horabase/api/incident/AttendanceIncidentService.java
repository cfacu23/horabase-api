package com.horabase.api.incident;

import com.horabase.api.attendance.Attendance;
import com.horabase.api.attendance.AttendanceRepository;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.incident.dto.IncidentRequest;
import com.horabase.api.incident.dto.IncidentResponse;
import com.horabase.api.incident.dto.ResolveIncidentRequest;
import com.horabase.api.shift.Shift;
import com.horabase.api.shift.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceIncidentService {

    private final AttendanceIncidentRepository repository;
    private final BusinessRepository businesses;
    private final EmployeeRepository employees;
    private final ShiftRepository shifts;
    private final AttendanceRepository attendances;

    public AttendanceIncidentService(
            AttendanceIncidentRepository repository,
            BusinessRepository businesses,
            EmployeeRepository employees,
            ShiftRepository shifts,
            AttendanceRepository attendances
    ) {
        this.repository = repository;
        this.businesses = businesses;
        this.employees = employees;
        this.shifts = shifts;
        this.attendances = attendances;
    }

    @Transactional
    public IncidentResponse create(
            Long businessId,
            IncidentRequest request
    ) {
        Business business = findBusiness(businessId);
        Employee employee = findEmployee(businessId, request.employeeId());
        Shift shift = findOptionalShift(businessId, request.shiftId());
        Attendance attendance = findOptionalAttendance(
                businessId, request.attendanceId()
        );

        validateOwnership(employee, shift, attendance);

        AttendanceIncident incident = new AttendanceIncident();
        incident.setBusiness(business);
        incident.setEmployee(employee);
        incident.setShift(shift);
        incident.setAttendance(attendance);
        incident.setIncidentDate(request.incidentDate());
        incident.setType(request.type());
        incident.setDetectedMinutes(request.detectedMinutes());
        incident.setNotes(normalize(request.notes()));
        incident.setStatus(IncidentStatus.DETECTED);

        return toResponse(repository.save(incident));
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> list(
            Long businessId,
            LocalDate from,
            LocalDate to,
            Long employeeId
    ) {
        findBusiness(businessId);
        if (to.isBefore(from)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El período no es válido"
            );
        }

        List<AttendanceIncident> result;
        if (employeeId == null) {
            result = repository
                    .findAllByBusiness_IdAndIncidentDateBetweenOrderByIncidentDateDesc(
                            businessId, from, to
                    );
        } else {
            findEmployee(businessId, employeeId);
            result = repository
                    .findAllByBusiness_IdAndEmployee_IdAndIncidentDateBetweenOrderByIncidentDateDesc(
                            businessId, employeeId, from, to
                    );
        }
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional
    public IncidentResponse resolve(
            Long businessId,
            Long incidentId,
            ResolveIncidentRequest request
    ) {
        if (request.status() == IncidentStatus.DETECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El estado de resolución no es válido"
            );
        }

        AttendanceIncident incident = repository
                .findByIdAndBusiness_Id(incidentId, businessId)
                .orElseThrow(() -> notFound("incidencia"));
        incident.setStatus(request.status());

        String note = normalize(request.notes());
        if (note != null) {
            incident.setNotes(incident.getNotes() == null
                    ? note
                    : incident.getNotes() + System.lineSeparator() + note);
        }
        return toResponse(repository.save(incident));
    }

    private Business findBusiness(Long businessId) {
        return businesses.findById(businessId)
                .orElseThrow(() -> notFound("comercio"));
    }

    private Employee findEmployee(Long businessId, Long employeeId) {
        return employees.findByIdAndBusiness_Id(employeeId, businessId)
                .orElseThrow(() -> notFound("empleado"));
    }

    private Shift findOptionalShift(Long businessId, Long shiftId) {
        if (shiftId == null) {
            return null;
        }
        return shifts.findByIdAndBusiness_Id(shiftId, businessId)
                .orElseThrow(() -> notFound("turno"));
    }

    private Attendance findOptionalAttendance(
            Long businessId,
            Long attendanceId
    ) {
        if (attendanceId == null) {
            return null;
        }
        return attendances
                .findDetailedByIdAndBusinessId(attendanceId, businessId)
                .orElseThrow(() -> notFound("asistencia"));
    }

    private void validateOwnership(
            Employee employee,
            Shift shift,
            Attendance attendance
    ) {
        boolean invalidShift = shift != null
                && !shift.getEmployee().getId().equals(employee.getId());
        boolean invalidAttendance = attendance != null
                && !attendance.getEmployee().getId().equals(employee.getId());
        if (invalidShift || invalidAttendance) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El turno o la asistencia no pertenece al empleado"
            );
        }
    }

    private ResponseStatusException notFound(String resource) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se encontró el " + resource
        );
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private IncidentResponse toResponse(AttendanceIncident incident) {
        Employee employee = incident.getEmployee();
        return new IncidentResponse(
                incident.getId(),
                incident.getBusiness().getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                incident.getShift() == null ? null : incident.getShift().getId(),
                incident.getAttendance() == null
                        ? null : incident.getAttendance().getId(),
                incident.getIncidentDate(),
                incident.getType(),
                incident.getStatus(),
                incident.getDetectedMinutes(),
                incident.getNotes(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }
}
