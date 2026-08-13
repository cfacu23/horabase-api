package com.horabase.api.overtime;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OvertimeRepository extends JpaRepository<OvertimeRecord, Long> {
    Optional<OvertimeRecord> findByIdAndBusiness_Id(Long id, Long businessId);
    List<OvertimeRecord> findAllByBusiness_IdAndWorkDateBetweenOrderByWorkDateDesc(
            Long businessId, LocalDate from, LocalDate to);
    List<OvertimeRecord> findAllByBusiness_IdAndEmployee_IdAndWorkDateBetweenOrderByWorkDateDesc(
            Long businessId, Long employeeId, LocalDate from, LocalDate to);
}
