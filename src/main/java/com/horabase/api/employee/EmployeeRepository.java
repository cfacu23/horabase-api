package com.horabase.api.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByBusiness_IdOrderByLastNameAscFirstNameAsc(
            Long businessId
    );

    Optional<Employee> findByIdAndBusiness_Id(
            Long employeeId,
            Long businessId
    );

    Optional<Employee> findByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);
}