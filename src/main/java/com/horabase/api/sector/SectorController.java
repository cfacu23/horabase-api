package com.horabase.api.sector;

import com.horabase.api.sector.dto.CreateSectorRequest;
import com.horabase.api.sector.dto.SectorResponse;
import com.horabase.api.sector.dto.UpdateSectorRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses/{businessId}/sectors")
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class SectorController {

    private final SectorService sectorService;

    public SectorController(SectorService sectorService) {
        this.sectorService = sectorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SectorResponse create(
            @PathVariable Long businessId,
            @Valid @RequestBody CreateSectorRequest request
    ) {
        return sectorService.create(businessId, request);
    }

    @GetMapping
    public List<SectorResponse> findAll(
            @PathVariable Long businessId
    ) {
        return sectorService.findAllByBusiness(businessId);
    }

    @GetMapping("/{sectorId}")
    public SectorResponse findById(
            @PathVariable Long businessId,
            @PathVariable Long sectorId
    ) {
        return sectorService.findById(businessId, sectorId);
    }

    @PutMapping("/{sectorId}")
    public SectorResponse update(
            @PathVariable Long businessId,
            @PathVariable Long sectorId,
            @Valid @RequestBody UpdateSectorRequest request
    ) {
        return sectorService.update(
                businessId,
                sectorId,
                request
        );
    }
}
