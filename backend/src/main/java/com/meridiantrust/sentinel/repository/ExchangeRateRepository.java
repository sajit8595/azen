package com.meridiantrust.sentinel.repository;

import com.meridiantrust.sentinel.domain.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByCurrency(String currency);
}
