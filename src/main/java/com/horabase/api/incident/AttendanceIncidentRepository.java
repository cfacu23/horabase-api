package com.horabase.api.incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate; import java.util.*;
public interface AttendanceIncidentRepository extends JpaRepository<AttendanceIncident,Long>{
 Optional<AttendanceIncident> findByIdAndBusiness_Id(Long id,Long businessId);
 List<AttendanceIncident> findAllByBusiness_IdAndIncidentDateBetweenOrderByIncidentDateDesc(Long businessId,LocalDate from,LocalDate to);
 List<AttendanceIncident> findAllByBusiness_IdAndEmployee_IdAndIncidentDateBetweenOrderByIncidentDateDesc(Long businessId,Long employeeId,LocalDate from,LocalDate to);
 long countByBusiness_IdAndIncidentDateAndTypeAndStatusNot(Long businessId,LocalDate date,IncidentType type,IncidentStatus status);
}
