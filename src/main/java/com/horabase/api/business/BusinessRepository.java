package com.horabase.api.business;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByTaxId(String taxId);

    boolean existsByTaxId(String taxId);
}