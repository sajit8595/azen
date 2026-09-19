package com.meridiantrust.sentinel.repository;

import com.meridiantrust.sentinel.domain.HighRiskJurisdiction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HighRiskJurisdictionRepository extends JpaRepository<HighRiskJurisdiction, Long> {
    Optional<HighRiskJurisdiction> findByCountryCodeAndActiveTrue(String countryCode);
}
