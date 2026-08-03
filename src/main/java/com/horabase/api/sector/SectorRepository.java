package com.horabase.api.sector;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findAllByBusiness_IdOrderByNameAsc(Long businessId);

    Optional<Sector> findByIdAndBusiness_Id(
            Long id,
            Long businessId
    );

    boolean existsByBusiness_IdAndNameIgnoreCase(
            Long businessId,
            String name
    );

    boolean existsByBusiness_IdAndNameIgnoreCaseAndIdNot(
            Long businessId,
            String name,
            Long id
    );
}