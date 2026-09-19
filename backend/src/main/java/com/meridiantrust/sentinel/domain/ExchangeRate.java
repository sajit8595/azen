package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rate")
@Getter
@Setter
public class ExchangeRate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String currency;

    @Column(name = "rate_to_inr", nullable = false)
    private BigDecimal rateToInr;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
