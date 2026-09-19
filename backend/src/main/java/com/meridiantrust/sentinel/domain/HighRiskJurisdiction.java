package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "high_risk_jurisdiction")
@Getter
@Setter
public class HighRiskJurisdiction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", nullable = false, unique = true)
    private String countryCode;

    private String countryName;
    private String reason;
    private boolean active = true;
}
