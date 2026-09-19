package com.meridiantrust.sentinel.detection;

import com.meridiantrust.sentinel.domain.Alert;
import com.meridiantrust.sentinel.repository.AlertRepository;
import com.meridiantrust.sentinel.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Persists alerts with de-duplication in its own transaction. Kept as a
 * separate collaborator (not a private method on DetectionEngine) so the
 * {@code REQUIRES_NEW} transaction boundary is honoured — Spring's proxy-based
 * transactions do not apply to self-invoked methods.
 *
 * <p>Concurrency: de-duplication is guarded both by an existence check and by
 * the unique {@code dedup_key} DB constraint, so concurrent transaction streams
 * cannot create duplicate alerts; a lost race surfaces as a
 * {@link DataIntegrityViolationException} and is treated as "already alerted".
 */
@Component
public class AlertPersister {

    private static final Logger log = LoggerFactory.getLogger(AlertPersister.class);

    private final AlertRepository alertRepository;
    private final AuditService auditService;

    public AlertPersister(AlertRepository alertRepository, AuditService auditService) {
        this.alertRepository = alertRepository;
        this.auditService = auditService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Alert> persistIfNew(RuleResult r) {
        if (alertRepository.existsByDedupKey(r.dedupKey())) {
            return Optional.empty();
        }
        Alert alert = new Alert();
        alert.setCustomerId(r.customerId());
        alert.setAccountId(r.accountId());
        alert.setRuleCode(r.ruleCode());
        alert.setRuleName(r.ruleName());
        alert.setRiskScore(r.riskScore());
        alert.setSeverity(severityFor(r.riskScore()));
        alert.setStatus(Alert.Status.OPEN);
        alert.setExplanation(r.explanation());
        alert.setEvidenceTxnIds(String.join(",", r.evidenceTxnIds()));
        alert.setDedupKey(r.dedupKey());
        try {
            Alert saved = alertRepository.save(alert);
            auditService.record("ALERT", String.valueOf(saved.getId()), "CREATED", "SYSTEM",
                    "{\"rule\":\"" + r.ruleCode() + "\",\"score\":" + r.riskScore() + "}");
            log.info("Alert created: rule={} score={} customer={}", r.ruleCode(), r.riskScore(), r.customerId());
            return Optional.of(saved);
        } catch (DataIntegrityViolationException dup) {
            // Concurrent insert lost the race on the unique dedup_key — de-duplicated.
            log.debug("Duplicate alert suppressed for dedupKey={}", r.dedupKey());
            return Optional.empty();
        }
    }

    private Alert.Severity severityFor(int score) {
        if (score >= 90) return Alert.Severity.CRITICAL;
        if (score >= 75) return Alert.Severity.HIGH;
        if (score >= 50) return Alert.Severity.MEDIUM;
        return Alert.Severity.LOW;
    }
}
