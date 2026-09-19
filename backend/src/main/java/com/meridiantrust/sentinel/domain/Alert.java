package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "alert")
@Getter
@Setter
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "rule_code", nullable = false)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "evidence_txn_ids", columnDefinition = "TEXT")
    private String evidenceTxnIds;

    @Column(name = "dedup_key", nullable = false)
    private String dedupKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum Status { OPEN, IN_REVIEW, CLOSED }
}
