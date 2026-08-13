package com.horabase.api.terminal;

import com.horabase.api.business.Business;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.terminal.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class TerminalDeviceService {

    private final TerminalDeviceRepository terminalRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public TerminalDeviceService(
            TerminalDeviceRepository terminalRepository,
            BusinessRepository businessRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.terminalRepository = terminalRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TerminalDeviceCredentialsResponse create(
            Long businessId,
            CreateTerminalDeviceRequest request
    ) {
        Business business = findBusiness(businessId);

        if (!business.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El comercio está inactivo"
            );
        }

        String identifier = normalizeIdentifier(request.identifier());

        if (terminalRepository.existsByIdentifier(identifier)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un terminal con ese identificador"
            );
        }

        String secret = generateSecret();
        TerminalDevice terminal = new TerminalDevice();
        terminal.setBusiness(business);
        terminal.setName(request.name().trim());
        terminal.setIdentifier(identifier);
        terminal.setSecretHash(passwordEncoder.encode(secret));
        terminal.setActive(true);

        TerminalDevice saved = terminalRepository.save(terminal);
        return new TerminalDeviceCredentialsResponse(
                toResponse(saved),
                secret
        );
    }

    @Transactional(readOnly = true)
    public List<TerminalDeviceResponse> findAll(Long businessId) {
        findBusiness(businessId);
        return terminalRepository
                .findAllByBusiness_IdOrderByNameAsc(businessId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TerminalDeviceResponse update(
            Long businessId,
            Long terminalId,
            UpdateTerminalDeviceRequest request
    ) {
        TerminalDevice terminal = findTerminal(businessId, terminalId);
        terminal.setName(request.name().trim());
        terminal.setActive(request.active());
        return toResponse(terminalRepository.save(terminal));
    }

    @Transactional
    public TerminalDeviceCredentialsResponse rotateSecret(
            Long businessId,
            Long terminalId
    ) {
        TerminalDevice terminal = findTerminal(businessId, terminalId);
        String secret = generateSecret();
        terminal.setSecretHash(passwordEncoder.encode(secret));

        return new TerminalDeviceCredentialsResponse(
                toResponse(terminalRepository.save(terminal)),
                secret
        );
    }

    private Business findBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el comercio"
                ));
    }

    private TerminalDevice findTerminal(
            Long businessId,
            Long terminalId
    ) {
        return terminalRepository
                .findByIdAndBusiness_Id(terminalId, businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el terminal"
                ));
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes);
    }

    private String normalizeIdentifier(String identifier) {
        return identifier.trim().toLowerCase(Locale.ROOT);
    }

    private TerminalDeviceResponse toResponse(TerminalDevice terminal) {
        return new TerminalDeviceResponse(
                terminal.getId(),
                terminal.getBusiness().getId(),
                terminal.getName(),
                terminal.getIdentifier(),
                terminal.isActive(),
                terminal.getLastSeenAt(),
                terminal.getCreatedAt(),
                terminal.getUpdatedAt()
        );
    }
}
