package com.horabase.api.request;

import com.horabase.api.auth.CurrentUser;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.Employee;
import com.horabase.api.employee.EmployeeRepository;
import com.horabase.api.request.dto.CreateEmployeeRequest;
import com.horabase.api.request.dto.EmployeeRequestResponse;
import com.horabase.api.request.dto.ResolveEmployeeRequest;
import com.horabase.api.shift.Shift;
import com.horabase.api.shift.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EmployeeRequestService {

    private final EmployeeRequestRepository repository;
    private final BusinessRepository businesses;
    private final EmployeeRepository employees;
    private final ShiftRepository shifts;
    private final Clock clock;

    public EmployeeRequestService(
            EmployeeRequestRepository repository,
            BusinessRepository businesses,
            EmployeeRepository employees,
            ShiftRepository shifts,
            Clock clock
    ) {
        this.repository = repository;
        this.businesses = businesses;
        this.employees = employees;
        this.shifts = shifts;
        this.clock = clock;
    }

    @Transactional
    public EmployeeRequestResponse create(
            CurrentUser user,
            CreateEmployeeRequest request
    ) {
        requireEmployee(user);
        Business business = businesses.findById(user.businessId())
                .orElseThrow(this::notFound);
        Employee employee = employees
                .findByIdAndBusiness_Id(user.employeeId(), user.businessId())
                .orElseThrow(this::notFound);
        Shift shift = findOptionalShift(user.businessId(), request.shiftId());

        if (shift != null
                && !shift.getEmployee().getId().equals(employee.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El turno no pertenece al empleado"
            );
        }

        EmployeeRequest entity = new EmployeeRequest();
        entity.setBusiness(business);
        entity.setEmployee(employee);
        entity.setShift(shift);
        entity.setType(request.type());
        entity.setTitle(request.title().trim());
        entity.setDescription(request.description().trim());
        entity.setRelatedDate(request.relatedDate());
        entity.setStatus(EmployeeRequestStatus.PENDING);
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<EmployeeRequestResponse> mine(CurrentUser user) {
        requireEmployee(user);
        return repository
                .findAllByBusiness_IdAndEmployee_IdOrderByCreatedAtDesc(
                        user.businessId(), user.employeeId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeRequestResponse> list(
            Long businessId,
            EmployeeRequestStatus status
    ) {
        requireBusiness(businessId);
        List<EmployeeRequest> result = status == null
                ? repository.findAllByBusiness_IdOrderByCreatedAtDesc(businessId)
                : repository.findAllByBusiness_IdAndStatusOrderByCreatedAtAsc(
                        businessId, status
                );
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EmployeeRequestResponse> listForEmployee(
            Long businessId,
            Long employeeId
    ) {
        employees.findByIdAndBusiness_Id(employeeId, businessId)
                .orElseThrow(this::notFound);
        return repository
                .findAllByBusiness_IdAndEmployee_IdOrderByCreatedAtDesc(
                        businessId, employeeId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EmployeeRequestResponse resolve(
            Long businessId,
            Long requestId,
            EmployeeRequestStatus status,
            ResolveEmployeeRequest request
    ) {
        if (status != EmployeeRequestStatus.APPROVED
                && status != EmployeeRequestStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La resolución no es válida"
            );
        }

        EmployeeRequest entity = repository
                .findByIdAndBusiness_Id(requestId, businessId)
                .orElseThrow(this::notFound);
        if (entity.getStatus() != EmployeeRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La solicitud ya fue resuelta"
            );
        }

        entity.setStatus(status);
        entity.setAdminResponse(request.response().trim());
        entity.setResolvedAt(OffsetDateTime.now(clock));
        return toResponse(repository.save(entity));
    }

    @Transactional
    public EmployeeRequestResponse cancel(CurrentUser user, Long requestId) {
        requireEmployee(user);
        EmployeeRequest entity = repository
                .findByIdAndBusiness_Id(requestId, user.businessId())
                .filter(request -> request.getEmployee().getId()
                        .equals(user.employeeId()))
                .orElseThrow(this::notFound);
        if (entity.getStatus() != EmployeeRequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden cancelar solicitudes pendientes"
            );
        }

        entity.setStatus(EmployeeRequestStatus.CANCELLED);
        entity.setResolvedAt(OffsetDateTime.now(clock));
        return toResponse(repository.save(entity));
    }

    private void requireEmployee(CurrentUser user) {
        if (user.employeeId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La cuenta no pertenece a un empleado"
            );
        }
    }

    private void requireBusiness(Long businessId) {
        if (!businesses.existsById(businessId)) {
            throw notFound();
        }
    }

    private Shift findOptionalShift(Long businessId, Long shiftId) {
        if (shiftId == null) {
            return null;
        }
        return shifts.findByIdAndBusiness_Id(shiftId, businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el turno"
                ));
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No se encontró la solicitud"
        );
    }

    private EmployeeRequestResponse toResponse(EmployeeRequest request) {
        Employee employee = request.getEmployee();
        return new EmployeeRequestResponse(
                request.getId(),
                request.getBusiness().getId(),
                employee.getId(),
                employee.getFirstName() + " " + employee.getLastName(),
                request.getType(),
                request.getTitle(),
                request.getDescription(),
                request.getShift() == null ? null : request.getShift().getId(),
                request.getRelatedDate(),
                request.getStatus(),
                request.getAdminResponse(),
                request.getResolvedAt(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
