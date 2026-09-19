package com.meridiantrust.sentinel.repository;

import com.meridiantrust.sentinel.domain.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RuleConfigRepository extends JpaRepository<RuleConfig, Long> {
    Optional<RuleConfig> findByRuleCode(String ruleCode);
}
