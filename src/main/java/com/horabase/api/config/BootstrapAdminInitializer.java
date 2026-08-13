package com.horabase.api.config;

import com.horabase.api.account.AccountRepository;
import com.horabase.api.account.AccountService;
import com.horabase.api.account.dto.CreateAdminAccountRequest;
import com.horabase.api.business.BusinessRepository;
import com.horabase.api.business.BusinessService;
import com.horabase.api.business.dto.CreateBusinessRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final BusinessRepository businesses;
    private final AccountRepository accounts;
    private final BusinessService businessService;
    private final AccountService accountService;
    private final boolean enabled;
    private final String businessName;
    private final String taxId;
    private final String adminDocument;
    private final String adminEmail;
    private final String adminPassword;

    public BootstrapAdminInitializer(
            BusinessRepository businesses,
            AccountRepository accounts,
            BusinessService businessService,
            AccountService accountService,
            @Value("${horabase.bootstrap.enabled:false}") boolean enabled,
            @Value("${horabase.bootstrap.business-name:}") String businessName,
            @Value("${horabase.bootstrap.tax-id:}") String taxId,
            @Value("${horabase.bootstrap.admin-document:}") String adminDocument,
            @Value("${horabase.bootstrap.admin-email:}") String adminEmail,
            @Value("${horabase.bootstrap.admin-password:}") String adminPassword
    ) {
        this.businesses = businesses;
        this.accounts = accounts;
        this.businessService = businessService;
        this.accountService = accountService;
        this.enabled = enabled;
        this.businessName = businessName;
        this.taxId = taxId;
        this.adminDocument = adminDocument;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled || businesses.count() > 0 || accounts.count() > 0) {
            return;
        }
        validateConfiguration();

        var business = businessService.create(new CreateBusinessRequest(
                businessName, taxId, null, null, adminEmail
        ));
        accountService.createAdmin(
                business.id(),
                new CreateAdminAccountRequest(
                        adminDocument, adminEmail, adminPassword
                )
        );
    }

    private void validateConfiguration() {
        if (businessName.isBlank()
                || taxId.isBlank()
                || adminDocument.isBlank()
                || adminEmail.isBlank()
                || adminPassword.length() < 8) {
            throw new IllegalStateException(
                    "Las variables HORABASE_BOOTSTRAP_* son incompletas"
            );
        }
    }
}
