package com.horabase.api.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByBusiness_IdAndDocument(
            Long businessId,
            String document
    );

    Optional<Account> findByBusiness_IdAndEmailIgnoreCase(
            Long businessId,
            String email
    );

    List<Account> findAllByDocument(String document);

    List<Account> findAllByEmailIgnoreCase(String email);

    boolean existsByBusiness_Id(Long businessId);

    boolean existsByIdAndActiveTrueAndBusiness_IdAndBusiness_ActiveTrue(
            Long accountId,
            Long businessId
    );

    boolean existsByBusiness_IdAndDocument(
            Long businessId,
            String document
    );

    boolean existsByBusiness_IdAndEmailIgnoreCase(
            Long businessId,
            String email
    );

    boolean existsByBusiness_IdAndEmailIgnoreCaseAndIdNot(
            Long businessId,
            String email,
            Long accountId
    );
}
