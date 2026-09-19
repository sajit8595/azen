package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
public class Customer {
    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
    private String city;
    private String state;
    private String country;
    private String occupation;
    private BigDecimal annualIncome;
    private String customerSegment;
    private String kycStatus;
    private String riskRating;

    @Column(name = "is_politically_exposed")
    private boolean politicallyExposed;

    private LocalDate customerSince;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
