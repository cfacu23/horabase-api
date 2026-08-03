package com.horabase.api.employee;

import com.horabase.api.account.Account;
import com.horabase.api.account.AccountRepository;
import com.horabase.api.account.Role;
import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.employee.dto.CreateEmployeeRequest;
import com.horabase.api.employee.dto.EmployeeResponse;
import com.horabase.api.sector.Sector;
import com.horabase.api.sector.SectorRepository;
import com.horabase.api.employee.dto.UpdateEmployeeRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final BusinessRepository businessRepository;
    private final SectorRepository sectorRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            AccountRepository accountRepository,
            BusinessRepository businessRepository,
            SectorRepository sectorRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.businessRepository = businessRepository;
        this.sectorRepository = sectorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public EmployeeResponse create(
            Long businessId,
            CreateEmployeeRequest request
    ) {
        Business business = findBusinessById(businessId);

        if (!business.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pueden crear empleados en un comercio inactivo"
            );
        }

        Sector sector = findSectorById(
                businessId,
                request.sectorId()
        );

        if (!sector.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede asignar un empleado a un sector inactivo"
            );
        }

        String normalizedDocument =
                normalizeDocument(request.document());

        String normalizedEmail =
                normalizeEmail(request.email());

        if (normalizedDocument.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cédula no tiene un formato válido"
            );
        }

        if (accountRepository.existsByBusiness_IdAndDocument(
                businessId,
                normalizedDocument
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cuenta con esa cédula"
            );
        }

        if (accountRepository.existsByBusiness_IdAndEmailIgnoreCase(
                businessId,
                normalizedEmail
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una cuenta con ese correo"
            );
        }

        Account account = new Account();
        account.setBusiness(business);
        account.setDocument(normalizedDocument);
        account.setEmail(normalizedEmail);
        account.setPasswordHash(
                passwordEncoder.encode(request.temporaryPassword())
        );
        account.setRole(Role.EMPLOYEE);
        account.setActive(true);
        account.setMustChangePassword(true);

        Account savedAccount = accountRepository.save(account);

        Employee employee = new Employee();
        employee.setBusiness(business);
        employee.setAccount(savedAccount);
        employee.setSector(sector);
        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setPhone(normalizeNullable(request.phone()));
        employee.setHireDate(request.hireDate());
        employee.setHourlyRate(request.hourlyRate());
        employee.setOvertimeHourlyRate(
                request.overtimeHourlyRate()
        );
        employee.setWeeklyHours(request.weeklyHours());
        employee.setActive(true);

        return toResponse(employeeRepository.save(employee));
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAllByBusiness(
            Long businessId
    ) {
        findBusinessById(businessId);

        return employeeRepository
                .findAllByBusiness_IdOrderByLastNameAscFirstNameAsc(
                        businessId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmployeeResponse findById(
            Long businessId,
            Long employeeId
    ) {
        return toResponse(
                findEmployeeById(businessId, employeeId)
        );
    }

    @Transactional
    public EmployeeResponse update(
            Long businessId,
            Long employeeId,
            UpdateEmployeeRequest request
    ) {
        Employee employee = findEmployeeById(
                businessId,
                employeeId
        );

        Account account = employee.getAccount();

        Sector sector = findSectorById(
                businessId,
                request.sectorId()
        );

        if (!sector.isActive()
                && !sector.getId().equals(employee.getSector().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede asignar un sector inactivo"
            );
        }

        String normalizedEmail = normalizeEmail(request.email());

        if (accountRepository
                .existsByBusiness_IdAndEmailIgnoreCaseAndIdNot(
                        businessId,
                        normalizedEmail,
                        account.getId()
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otra cuenta con ese correo"
            );
        }

        account.setEmail(normalizedEmail);
        account.setActive(request.active());

        employee.setFirstName(request.firstName().trim());
        employee.setLastName(request.lastName().trim());
        employee.setPhone(normalizeNullable(request.phone()));
        employee.setSector(sector);
        employee.setHireDate(request.hireDate());
        employee.setHourlyRate(request.hourlyRate());
        employee.setOvertimeHourlyRate(
                request.overtimeHourlyRate()
        );
        employee.setWeeklyHours(request.weeklyHours());
        employee.setActive(request.active());

        accountRepository.save(account);

        return toResponse(employeeRepository.save(employee));
    }

    private Business findBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el comercio"
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
                        "No se encontró el sector dentro del comercio"
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

    private String normalizeDocument(String document) {
        return document.replaceAll("[^0-9]", "");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private EmployeeResponse toResponse(Employee employee) {
        Account account = employee.getAccount();
        Sector sector = employee.getSector();

        return new EmployeeResponse(
                employee.getId(),
                employee.getBusiness().getId(),
                account.getId(),
                sector.getId(),
                sector.getName(),
                employee.getFirstName(),
                employee.getLastName(),
                account.getDocument(),
                account.getEmail(),
                employee.getPhone(),
                employee.getHireDate(),
                employee.getHourlyRate(),
                employee.getOvertimeHourlyRate(),
                employee.getWeeklyHours(),
                employee.isActive(),
                account.isMustChangePassword(),
                employee.getCreatedAt(),
                employee.getUpdatedAt()
        );
    }
}