package com.horabase.api.business;

import com.horabase.api.business.dto.BusinessResponse;
import com.horabase.api.business.dto.CreateBusinessRequest;
import com.horabase.api.business.dto.UpdateBusinessRequest;
import jakarta.validation.Valid;
import com.horabase.api.auth.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public List<BusinessResponse> findAll(@AuthenticationPrincipal Jwt jwt) {
        Long businessId = CurrentUser.from(jwt).businessId();
        return List.of(businessService.findById(businessId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessSecurity.canAccess(authentication, #id)")
    public BusinessResponse findById(@PathVariable Long id) {
        return businessService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@businessSecurity.canAccess(authentication, #id)")
    public BusinessResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBusinessRequest request
    ) {
        return businessService.update(id, request);
    }
}
