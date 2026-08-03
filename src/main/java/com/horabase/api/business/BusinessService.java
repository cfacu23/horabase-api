package com.horabase.api.business;

import com.horabase.api.business.dto.BusinessResponse;
import com.horabase.api.business.dto.CreateBusinessRequest;
import com.horabase.api.business.dto.UpdateBusinessRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Transactional
    public BusinessResponse create(CreateBusinessRequest request) {
        String normalizedTaxId = request.taxId().trim();

        if (businessRepository.existsByTaxId(normalizedTaxId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un comercio con ese RUT"
            );
        }

        Business business = new Business();
        business.setName(request.name().trim());
        business.setTaxId(normalizedTaxId);
        business.setAddress(request.address());
        business.setPhone(request.phone());
        business.setEmail(request.email());

        return toResponse(businessRepository.save(business));
    }

    @Transactional(readOnly = true)
    public List<BusinessResponse> findAll() {
        return businessRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BusinessResponse findById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public BusinessResponse update(Long id, UpdateBusinessRequest request) {
        Business business = findEntityById(id);

        business.setName(request.name().trim());
        business.setAddress(request.address());
        business.setPhone(request.phone());
        business.setEmail(request.email());
        business.setActive(request.active());

        return toResponse(businessRepository.save(business));
    }

    private Business findEntityById(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el comercio"
                ));
    }

    private BusinessResponse toResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getName(),
                business.getTaxId(),
                business.getAddress(),
                business.getPhone(),
                business.getEmail(),
                business.isActive(),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }
}