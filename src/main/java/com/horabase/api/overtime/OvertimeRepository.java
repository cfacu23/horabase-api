package com.horabase.api.overtime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OvertimeRepository extends JpaRepository<OvertimeRecord, Long> {
    @EntityGraph(attributePaths = {"employee", "attendance"})
    Optional<OvertimeRecord> findByIdAndBusiness_Id(Long id, Long businessId);
    @EntityGraph(attributePaths = {"employee", "attendance"})
    List<OvertimeRecord> findAllByBusiness_IdAndWorkDateBetweenOrderByWorkDateDesc(
            Long businessId, LocalDate from, LocalDate to);
    @EntityGraph(attributePaths = {"employee", "attendance"})
    List<OvertimeRecord> findAllByBusiness_IdAndEmployee_IdAndWorkDateBetweenOrderByWorkDateDesc(
            Long businessId, Long employeeId, LocalDate from, LocalDate to);
    long countByBusiness_IdAndStatus(Long businessId, OvertimeStatus status);
}
