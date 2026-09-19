package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@Setter
public class Account {
    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    private String accountType;
    private String accountStatus;
    private String currency;
    private LocalDate openDate;
    private LocalDate closeDate;
    private String branchCode;
    private String branchCity;
    private BigDecimal currentBalance;
    private String accountTier;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
