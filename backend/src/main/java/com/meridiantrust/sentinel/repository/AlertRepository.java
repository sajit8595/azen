package com.meridiantrust.sentinel.repository;

import com.meridiantrust.sentinel.domain.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    Optional<Alert> findByDedupKey(String dedupKey);
    boolean existsByDedupKey(String dedupKey);
    Page<Alert> findByStatus(Alert.Status status, Pageable pageable);
    Page<Alert> findByRiskScoreGreaterThanEqual(int minRiskScore, Pageable pageable);
    long countByStatus(Alert.Status status);
    long countBySeverity(Alert.Severity severity);
}
