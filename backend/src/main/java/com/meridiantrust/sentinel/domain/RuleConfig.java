package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rule_config")
@Getter
@Setter
public class RuleConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "base_score", nullable = false)
    private int baseScore;

    @Column(name = "params_json", nullable = false, columnDefinition = "TEXT")
    private String paramsJson;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
