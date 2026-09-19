package com.meridiantrust.sentinel.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "aml_case")
@Getter
@Setter
public class AmlCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_id", nullable = false)
    private Long alertId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NEW;

    private String assignedTo;

    @Enumerated(EnumType.STRING)
    private Disposition disposition;

    @Column(columnDefinition = "TEXT")
    private String dispositionReason;

    private String analystId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    public enum Status { NEW, INVESTIGATING, DISPOSED }
    public enum Disposition { CONFIRMED_SAR, FALSE_POSITIVE, CLEARED }
}
