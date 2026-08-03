package com.horabase.api.sector;

import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.sector.dto.CreateSectorRequest;
import com.horabase.api.sector.dto.SectorResponse;
import com.horabase.api.sector.dto.UpdateSectorRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SectorService {

    private final SectorRepository sectorRepository;
    private final BusinessRepository businessRepository;

    public SectorService(
            SectorRepository sectorRepository,
            BusinessRepository businessRepository
    ) {
        this.sectorRepository = sectorRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public SectorResponse create(
            Long businessId,
            CreateSectorRequest request
    ) {
        Business business = findBusinessById(businessId);
        String normalizedName = request.name().trim();

        if (!business.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pueden crear sectores en un comercio inactivo"
            );
        }

        if (sectorRepository.existsByBusiness_IdAndNameIgnoreCase(
                businessId,
                normalizedName
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un sector con ese nombre"
            );
        }

        Sector sector = new Sector();
        sector.setBusiness(business);
        sector.setName(normalizedName);
        sector.setDescription(normalizeNullable(request.description()));

        return toResponse(sectorRepository.save(sector));
    }

    @Transactional(readOnly = true)
    public List<SectorResponse> findAllByBusiness(Long businessId) {
        findBusinessById(businessId);

        return sectorRepository
                .findAllByBusiness_IdOrderByNameAsc(businessId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SectorResponse findById(
            Long businessId,
            Long sectorId
    ) {
        return toResponse(findSectorById(businessId, sectorId));
    }

    @Transactional
    public SectorResponse update(
            Long businessId,
            Long sectorId,
            UpdateSectorRequest request
    ) {
        Sector sector = findSectorById(businessId, sectorId);
        String normalizedName = request.name().trim();

        if (sectorRepository
                .existsByBusiness_IdAndNameIgnoreCaseAndIdNot(
                        businessId,
                        normalizedName,
                        sectorId
                )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe otro sector con ese nombre"
            );
        }

        sector.setName(normalizedName);
        sector.setDescription(normalizeNullable(request.description()));
        sector.setActive(request.active());

        return toResponse(sectorRepository.save(sector));
    }

    private Business findBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el comercio"
                ));
    }

    private Sector findSectorById(
            Long businessId,
            Long sectorId
    ) {
        return sectorRepository
                .findByIdAndBusiness_Id(sectorId, businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el sector"
                ));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private SectorResponse toResponse(Sector sector) {
        return new SectorResponse(
                sector.getId(),
                sector.getBusiness().getId(),
                sector.getName(),
                sector.getDescription(),
                sector.isActive(),
                sector.getCreatedAt(),
                sector.getUpdatedAt()
        );
    }
}