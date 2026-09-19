package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.domain.Alert;
import com.meridiantrust.sentinel.domain.AmlCase;
import com.meridiantrust.sentinel.dto.Dtos.CaseView;
import com.meridiantrust.sentinel.dto.Dtos.DispositionRequest;
import com.meridiantrust.sentinel.repository.AlertRepository;
import com.meridiantrust.sentinel.repository.CaseRepository;
import com.meridiantrust.sentinel.web.error.ResourceNotFoundException;
import com.meridiantrust.sentinel.web.error.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CaseService {

    private final CaseRepository caseRepository;
    private final AlertRepository alertRepository;
    private final AuditService auditService;

    public CaseService(CaseRepository caseRepository, AlertRepository alertRepository, AuditService auditService) {
        this.caseRepository = caseRepository;
        this.alertRepository = alertRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CaseView createCase(Long alertId, String actor) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> ResourceNotFoundException.of("Alert", alertId));
        AmlCase existing = caseRepository.findByAlertId(alertId).orElse(null);
        if (existing != null) {
            return CaseView.from(existing);
        }
        AmlCase c = new AmlCase();
        c.setAlertId(alertId);
        c.setStatus(AmlCase.Status.NEW);
        c.setAssignedTo(actor);
        c.setUpdatedAt(LocalDateTime.now());
        AmlCase saved = caseRepository.save(c);

        // Move the underlying alert into review.
        alert.setStatus(Alert.Status.IN_REVIEW);
        alertRepository.save(alert);

        auditService.record("CASE", String.valueOf(saved.getId()), "CREATED", actor,
                "{\"alertId\":" + alertId + "}");
        return CaseView.from(saved);
    }

    public List<CaseView> list(String status) {
        List<AmlCase> cases = (status == null || status.isBlank())
                ? caseRepository.findAll()
                : caseRepository.findByStatus(AmlCase.Status.valueOf(status.toUpperCase()));
        return cases.stream().map(CaseView::from).toList();
    }

    public CaseView get(Long id) {
        return CaseView.from(caseRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Case", id)));
    }

    /**
     * Records analyst disposition. Cleared alerts are never deleted — the alert
     * stays with a disposition reason and analyst identity (Business Rule 6),
     * and the transition is written to the immutable audit log.
     */
    @Transactional
    public CaseView disposition(Long caseId, DispositionRequest request, String actor) {
        AmlCase c = caseRepository.findById(caseId)
                .orElseThrow(() -> ResourceNotFoundException.of("Case", caseId));
        if (request.reason() == null || request.reason().isBlank()) {
            throw new ValidationException("Disposition reason is required");
        }
        AmlCase.Disposition disp = AmlCase.Disposition.valueOf(request.disposition().toUpperCase());
        c.setDisposition(disp);
        c.setDispositionReason(request.reason());
        c.setAnalystId(actor);
        c.setStatus(AmlCase.Status.DISPOSED);
        c.setUpdatedAt(LocalDateTime.now());
        AmlCase saved = caseRepository.save(c);

        Alert alert = alertRepository.findById(c.getAlertId()).orElse(null);
        if (alert != null) {
            alert.setStatus(Alert.Status.CLOSED);
            alertRepository.save(alert);
        }
        auditService.record("CASE", String.valueOf(caseId), "DISPOSED", actor,
                "{\"disposition\":\"" + disp + "\",\"reason\":\"" + escape(request.reason()) + "\"}");
        return CaseView.from(saved);
    }

    private static String escape(String s) {
        return s.replace("\"", "'");
    }
}
