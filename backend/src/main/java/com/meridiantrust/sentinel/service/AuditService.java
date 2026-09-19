package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.domain.AuditLog;
import com.meridiantrust.sentinel.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Writes immutable audit entries for all alert/case state transitions
 * (Non-Functional Requirement: Auditability). Entries are append-only.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String entityType, String entityId, String action, String actorId, String detailsJson) {
        AuditLog entry = new AuditLog();
        entry.setEntityType(entityType);
        entry.setEntityId(entityId);
        entry.setAction(action);
        entry.setActorId(actorId);
        entry.setDetailsJson(detailsJson);
        auditLogRepository.save(entry);
    }

    public List<AuditLog> trail(String entityType, String entityId) {
        if (entityType == null && entityId == null) {
            return auditLogRepository.findAllByOrderByCreatedAtDesc();
        }
        if (entityType == null) {
            return auditLogRepository.findByEntityIdOrderByCreatedAtDesc(entityId);
        }
        if (entityId == null) {
            return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType);
        }
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }
}
