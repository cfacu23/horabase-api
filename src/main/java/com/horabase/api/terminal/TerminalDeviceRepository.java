package com.horabase.api.terminal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TerminalDeviceRepository
        extends JpaRepository<TerminalDevice, Long> {

    Optional<TerminalDevice> findByIdentifier(String identifier);

    @Query("""
            select t from TerminalDevice t
            join fetch t.business
            where t.identifier = :identifier
            """)
    Optional<TerminalDevice> findDetailedByIdentifier(
            @Param("identifier") String identifier
    );

    Optional<TerminalDevice> findByIdAndBusiness_Id(
            Long terminalId,
            Long businessId
    );

    List<TerminalDevice> findAllByBusiness_IdOrderByNameAsc(
            Long businessId
    );

    boolean existsByIdentifier(String identifier);
}
