package com.horabase.api.request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.*;
public interface EmployeeRequestRepository extends JpaRepository<EmployeeRequest,Long>{@EntityGraph(attributePaths={"employee","shift"}) Optional<EmployeeRequest>findByIdAndBusiness_Id(Long id,Long bid);@EntityGraph(attributePaths={"employee","shift"}) List<EmployeeRequest>findAllByBusiness_IdOrderByCreatedAtDesc(Long bid);@EntityGraph(attributePaths={"employee","shift"}) List<EmployeeRequest>findAllByBusiness_IdAndStatusOrderByCreatedAtAsc(Long bid,EmployeeRequestStatus status);@EntityGraph(attributePaths={"employee","shift"}) List<EmployeeRequest>findAllByBusiness_IdAndEmployee_IdOrderByCreatedAtDesc(Long bid,Long eid);long countByBusiness_IdAndStatus(Long bid,EmployeeRequestStatus status);}
