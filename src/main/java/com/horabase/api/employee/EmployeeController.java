package com.horabase.api.employee;

import com.horabase.api.employee.dto.CreateEmployeeRequest;
import com.horabase.api.employee.dto.EmployeeResponse;
import com.horabase.api.employee.dto.UpdateEmployeeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(
            @PathVariable Long businessId,
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return employeeService.create(businessId, request);
    }

    @GetMapping
    public List<EmployeeResponse> findAll(
            @PathVariable Long businessId
    ) {
        return employeeService.findAllByBusiness(businessId);
    }

    @GetMapping("/{employeeId}")
    public EmployeeResponse findById(
            @PathVariable Long businessId,
            @PathVariable Long employeeId
    ) {
        return employeeService.findById(
                businessId,
                employeeId
        );
    }

    @PutMapping("/{employeeId}")
    public EmployeeResponse update(
            @PathVariable Long businessId,
            @PathVariable Long employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return employeeService.update(
                businessId,
                employeeId,
                request
        );
    }
}