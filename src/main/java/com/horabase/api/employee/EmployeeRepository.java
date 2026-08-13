package com.horabase.api.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select e from Employee e
            join fetch e.account
            where e.id = :employeeId
              and e.business.id = :businessId
            """)
    Optional<Employee> findByIdAndBusinessIdForUpdate(
            @Param("employeeId") Long employeeId,
            @Param("businessId") Long businessId
    );

    Optional<Employee> findByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);

    long countByBusiness_IdAndActiveTrue(Long businessId);
}
