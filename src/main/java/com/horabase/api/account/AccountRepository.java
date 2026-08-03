package com.horabase.api.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByBusiness_IdAndDocument(
            Long businessId,
            String document
    );

    Optional<Account> findByBusiness_IdAndEmailIgnoreCase(
            Long businessId,
            String email
    );

    boolean existsByBusiness_IdAndDocument(
            Long businessId,
            String document
    );

    boolean existsByBusiness_IdAndEmailIgnoreCase(
            Long businessId,
            String email
    );
}