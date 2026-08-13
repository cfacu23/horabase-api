package com.horabase.api.incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.time.LocalDate; import java.util.*;
public interface AttendanceIncidentRepository extends JpaRepository<AttendanceIncident,Long>{
 @EntityGraph(attributePaths={"employee","shift","attendance"}) Optional<AttendanceIncident> findByIdAndBusiness_Id(Long id,Long businessId);
 @EntityGraph(attributePaths={"employee","shift","attendance"})
 List<AttendanceIncident> findAllByBusiness_IdAndIncidentDateBetweenOrderByIncidentDateDesc(Long businessId,LocalDate from,LocalDate to);
 @EntityGraph(attributePaths={"employee","shift","attendance"})
 List<AttendanceIncident> findAllByBusiness_IdAndEmployee_IdAndIncidentDateBetweenOrderByIncidentDateDesc(Long businessId,Long employeeId,LocalDate from,LocalDate to);
 long countByBusiness_IdAndIncidentDateAndTypeAndStatusNot(Long businessId,LocalDate date,IncidentType type,IncidentStatus status);
}
