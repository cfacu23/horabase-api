package com.horabase.api.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("""
            select a from Attendance a
            join fetch a.employee e
            left join fetch a.shift s
            where a.id = :attendanceId
              and a.business.id = :businessId
            """)
    Optional<Attendance> findDetailedByIdAndBusinessId(
            @Param("attendanceId") Long attendanceId,
            @Param("businessId") Long businessId
    );

    Optional<Attendance>
    findFirstByBusiness_IdAndEmployee_IdAndStatusOrderByCheckInAtDesc(
            Long businessId,
            Long employeeId,
            AttendanceStatus status
    );

    boolean existsByBusiness_IdAndEmployee_IdAndStatus(
            Long businessId,
            Long employeeId,
            AttendanceStatus status
    );

    @Query("""
            select a from Attendance a
            join fetch a.employee e
            left join fetch a.shift s
            where a.business.id = :businessId
              and a.checkInAt < :to
              and (a.checkOutAt is null or a.checkOutAt >= :from)
              and (:employeeId is null or e.id = :employeeId)
              and (:status is null or a.status = :status)
            order by a.checkInAt asc
            """)
    List<Attendance> findAllByPeriod(
            @Param("businessId") Long businessId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("employeeId") Long employeeId,
            @Param("status") AttendanceStatus status
    );

    long countByBusiness_IdAndStatus(
            Long businessId,
            AttendanceStatus status
    );
}
