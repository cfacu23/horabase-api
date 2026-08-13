package com.horabase.api.shift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    Optional<Shift> findByIdAndBusiness_Id(
            Long shiftId,
            Long businessId
    );

    @EntityGraph(attributePaths = {"employee", "sector"})
    List<Shift>
    findAllByBusiness_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
            Long businessId,
            OffsetDateTime periodEnd,
            OffsetDateTime periodStart
    );

    @EntityGraph(attributePaths = {"employee", "sector"})
    List<Shift>
    findAllByBusiness_IdAndEmployee_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
            Long businessId,
            Long employeeId,
            OffsetDateTime periodEnd,
            OffsetDateTime periodStart
    );

    @EntityGraph(attributePaths = {"employee", "sector"})
    List<Shift>
    findAllByBusiness_IdAndSector_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
            Long businessId,
            Long sectorId,
            OffsetDateTime periodEnd,
            OffsetDateTime periodStart
    );

    @EntityGraph(attributePaths = {"employee", "sector"})
    List<Shift>
    findAllByBusiness_IdAndEmployee_IdAndSector_IdAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
            Long businessId,
            Long employeeId,
            Long sectorId,
            OffsetDateTime periodEnd,
            OffsetDateTime periodStart
    );

    @EntityGraph(attributePaths = {"employee", "sector"})
    List<Shift>
    findAllByBusiness_IdAndEmployee_IdAndStatusAndStartsAtBetweenOrderByStartsAtAsc(
            Long businessId,
            Long employeeId,
            ShiftStatus status,
            OffsetDateTime from,
            OffsetDateTime to
    );

    boolean
    existsByEmployee_IdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusNot(
            Long employeeId,
            OffsetDateTime newShiftEnd,
            OffsetDateTime newShiftStart,
            ShiftStatus excludedStatus
    );

    boolean
    existsByEmployee_IdAndStartsAtLessThanAndEndsAtGreaterThanAndStatusNotAndIdNot(
            Long employeeId,
            OffsetDateTime newShiftEnd,
            OffsetDateTime newShiftStart,
            ShiftStatus excludedStatus,
            Long shiftId
    );

    long countByBusiness_IdAndStatusAndStartsAtBetween(
            Long businessId,
            ShiftStatus status,
            OffsetDateTime from,
            OffsetDateTime to
    );
}
