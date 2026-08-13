package com.horabase.api.account;

import com.horabase.api.account.dto.AccountResponse;
import com.horabase.api.account.dto.CreateAdminAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses/{businessId}/accounts")
@PreAuthorize("@businessSecurity.canAccess(authentication, #businessId)")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAdmin(
            @PathVariable Long businessId,
            @Valid @RequestBody CreateAdminAccountRequest request
    ) {
        return accountService.createAdmin(businessId, request);
    }
}
