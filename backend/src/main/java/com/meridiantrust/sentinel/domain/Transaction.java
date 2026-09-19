package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
public class Transaction {
    @Id
    private String id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency;

    @Column(name = "amount_inr", nullable = false)
    private BigDecimal amountInr;

    private String counterpartyName;
    private String counterpartyAccount;
    private String counterpartyCountry;
    private String channel;

    @Column(name = "txn_timestamp", nullable = false)
    private LocalDateTime txnTimestamp;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum Direction { CREDIT, DEBIT }
}
