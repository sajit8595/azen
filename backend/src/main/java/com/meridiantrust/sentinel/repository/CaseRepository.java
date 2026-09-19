package com.meridiantrust.sentinel.repository;

import com.meridiantrust.sentinel.domain.AmlCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CaseRepository extends JpaRepository<AmlCase, Long> {
    List<AmlCase> findByStatus(AmlCase.Status status);
    Optional<AmlCase> findByAlertId(Long alertId);
}
