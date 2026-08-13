package com.horabase.api.shift;

import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.sector.Sector;
import com.horabase.api.sector.SectorRepository;
import com.horabase.api.shift.dto.CreateShiftRequest;
import com.horabase.api.shift.dto.ShiftResponse;
import com.horabase.api.shift.dto.UpdateShiftRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final BusinessRepository businessRepository;
    private final EmployeeRepository employeeRepository;
    private final SectorRepository sectorRepository;

    public ShiftService(
            ShiftRepository shiftRepository,
            BusinessRepository businessRepository,
            EmployeeRepository employeeRepository,
            SectorRepository sectorRepository
    ) {
        this.shiftRepository = shiftRepository;
        this.businessRepository = businessRepository;
        this.employeeRepository = employeeRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional
    public ShiftResponse create(
            Long businessId,
            CreateShiftRequest request
    ) {
        Business business = findBusinessById(businessId);
        Employee employee = findEmployeeById(
                businessId,
                request.employeeId()
        );
        Sector sector = findSectorById(
                businessId,
                request.sectorId()
        );

        validateActiveResources(business, employee, sector);

        validatePeriod(
                request.startsAt(),
                request.endsAt(),
                request.breakMinutes()
        );

        if (hasOverlap(
                employee.getId(),
                request.startsAt(),
                request.endsAt()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El empleado ya tiene otro turno en ese horario"
            );
        }

        Shift shift = new Shift();
        shift.setBusiness(business);
        shift.setEmployee(employee);
        shift.setSector(sector);
        shift.setStartsAt(request.startsAt());
        shift.setEndsAt(request.endsAt());
        shift.setBreakMinutes(request.breakMinutes());
        shift.setNotes(normalizeNullable(request.notes()));
        shift.setStatus(ShiftStatus.SCHEDULED);

        return toResponse(shiftRepository.save(shift));
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> findAllByPeriod(
            Long businessId,
            OffsetDateTime from,
            OffsetDateTime to,
            Long employeeId
    ) {
        return findAllByPeriod(businessId, from, to, employeeId, null);
    }

    @Transactional(readOnly = true)
    public List<ShiftResponse> findAllByPeriod(
            Long businessId,
            OffsetDateTime from,
            OffsetDateTime to,
            Long employeeId,
            Long sectorId
    ) {
        findBusinessById(businessId);
        validateSearchPeriod(from, to);

        if (employeeId != null) {
            findEmployeeById(businessId, employeeId);
        }
        if (sectorId != null) {
            findSectorById(businessId, sectorId);
        }

        List<Shift> shifts;

        if (employeeId == null && sectorId == null) {
            shifts = shiftRepository
                    .findAllByBusiness_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
                            businessId,
                            to,
                            from
                    );
        } else if (employeeId != null && sectorId == null) {
            shifts = shiftRepository
                    .findAllByBusiness_IdAndEmployee_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
                            businessId,
                            employeeId,
                            to,
                            from
                    );
        } else if (employeeId == null) {
            shifts = shiftRepository
                    .findAllByBusiness_IdAndSector_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
                            businessId, sectorId, to, from
                    );
        } else {
            shifts = shiftRepository
                    .findAllByBusiness_IdAndEmployee_IdAndSector_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
                            businessId, employeeId, sectorId, to, from
                    );
        }

        return shifts.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShiftResponse findById(
            Long businessId,
            Long shiftId
    ) {
        return toResponse(findShiftById(businessId, shiftId));
    }

    @Transactional
    public ShiftResponse update(
            Long businessId,
            Long shiftId,
            UpdateShiftRequest request
    ) {
        Shift shift = findShiftById(businessId, shiftId);

        Employee employee = findEmployeeById(
                businessId,
                request.employeeId()
        );

        Sector sector = findSectorById(
                businessId,
                request.sectorId()
        );

        validatePeriod(
                request.startsAt(),
                request.endsAt(),
                request.breakMinutes()
        );

        if (request.status() == ShiftStatus.SCHEDULED) {
            validateActiveResources(
                    shift.getBusiness(),
                    employee,
                    sector
            );

            boolean overlaps = shiftRepository
                    .existsByEmployee_IdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusNotAndIdNot(
                            employee.getId(),
                            request.endsAt(),
                            request.startsAt(),
                            ShiftStatus.CANCELLED,
                            shiftId
                    );

            if (overlaps) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El empleado ya tiene otro turno en ese horario"
                );
            }
        }

        shift.setEmployee(employee);
        shift.setSector(sector);
        shift.setStartsAt(request.startsAt());
        shift.setEndsAt(request.endsAt());
        shift.setBreakMinutes(request.breakMinutes());
        shift.setNotes(normalizeNullable(request.notes()));
        shift.setStatus(request.status());

        return toResponse(shiftRepository.save(shift));
    }

    private boolean hasOverlap(
            Long employeeId,
            OffsetDateTime startsAt,
            OffsetDateTime endsAt
    ) {
        return shiftRepository
                .existsByEmployee_IdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusNot(
                        employeeId,
                        endsAt,
                        startsAt,
                        ShiftStatus.CANCELLED
                );
    }

    private void validatePeriod(
            OffsetDateTime startsAt,
            OffsetDateTime endsAt,
            int breakMinutes
    ) {
        if (!endsAt.isAfter(startsAt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La hora de salida debe ser posterior a la entrada"
            );
        }

        long totalMinutes = Duration
                .between(startsAt, endsAt)
                .toMinutes();

        if (breakMinutes >= totalMinutes) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El descanso debe ser menor que la duración del turno"
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

    private void validateActiveResources(
            Business business,
            Employee employee,
            Sector sector
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

        if (!sector.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El sector está inactivo"
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

    private Sector findSectorById(
            Long businessId,
            Long sectorId
    ) {
        return sectorRepository
                .findByIdAndBusiness_Id(sectorId, businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el sector"
                ));
    }

    private Shift findShiftById(
            Long businessId,
            Long shiftId
    ) {
        return shiftRepository
                .findByIdAndBusiness_Id(shiftId, businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el turno"
                ));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private ShiftResponse toResponse(Shift shift) {
        long plannedWorkMinutes = Duration
                .between(shift.getStartsAt(), shift.getEndsAt())
                .toMinutes() - shift.getBreakMinutes();

        Employee employee = shift.getEmployee();
        Sector sector = shift.getSector();

        return new ShiftResponse(
                shift.getId(),
                shift.getBusiness().getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                sector.getId(),
                sector.getName(),
                shift.getStartsAt(),
                shift.getEndsAt(),
                shift.getBreakMinutes(),
                plannedWorkMinutes,
                shift.getNotes(),
                shift.getStatus(),
                shift.getCreatedAt(),
                shift.getUpdatedAt()
        );
    }
}
