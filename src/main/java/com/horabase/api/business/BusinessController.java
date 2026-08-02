package com.horabase.api.business;

import com.horabase.api.business.dto.BusinessResponse;
import com.horabase.api.business.dto.CreateBusinessRequest;
import com.horabase.api.business.dto.UpdateBusinessRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessResponse create(
            @Valid @RequestBody CreateBusinessRequest request
    ) {
        return businessService.create(request);
    }

    @GetMapping
    public List<BusinessResponse> findAll() {
        return businessService.findAll();
    }

    @GetMapping("/{id}")
    public BusinessResponse findById(@PathVariable Long id) {
        return businessService.findById(id);
    }

    @PutMapping("/{id}")
    public BusinessResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBusinessRequest request
    ) {
        return businessService.update(id, request);
    }
}