package com.meridiantrust.sentinel.detection;

import com.meridiantrust.sentinel.domain.Alert;
import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.domain.Transaction;
import com.meridiantrust.sentinel.repository.AccountRepository;
import com.meridiantrust.sentinel.repository.HighRiskJurisdictionRepository;
import com.meridiantrust.sentinel.repository.RuleConfigRepository;
import com.meridiantrust.sentinel.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the detection rules against transactions and produces
 * risk-scored, de-duplicated {@link Alert}s. Rule evaluation is read-only; the
 * actual alert persistence (with its own transaction and dedup guard) is
 * delegated to {@link AlertPersister}.
 *
 * <p>The engine holds no per-transaction mutable state, so it is safe to invoke
 * concurrently from both the bulk-ingestion path and the streaming endpoint.
 */
@Service
public class DetectionEngine {

    private static final Logger log = LoggerFactory.getLogger(DetectionEngine.class);

    private final List<DetectionRule> rules;
    private final RuleConfigRepository ruleConfigRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final HighRiskJurisdictionRepository highRiskRepository;
    private final AlertPersister alertPersister;

    public DetectionEngine(List<DetectionRule> rules,
                           RuleConfigRepository ruleConfigRepository,
                           AccountRepository accountRepository,
                           TransactionRepository transactionRepository,
                           HighRiskJurisdictionRepository highRiskRepository,
                           AlertPersister alertPersister) {
        this.rules = rules;
        this.ruleConfigRepository = ruleConfigRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.highRiskRepository = highRiskRepository;
        this.alertPersister = alertPersister;
    }

    /** Evaluate a batch of transactions; returns the alerts that were created. */
    public List<Alert> evaluateBatch(List<Transaction> transactions) {
        List<Alert> created = new ArrayList<>();
        for (Transaction t : transactions) {
            created.addAll(evaluate(t));
        }
        return created;
    }

    /** Evaluate a single transaction against all enabled rules. */
    public List<Alert> evaluate(Transaction trigger) {
        DetectionContext ctx = new DetectionContext(accountRepository, transactionRepository, highRiskRepository);
        List<Alert> created = new ArrayList<>();
        for (DetectionRule rule : rules) {
            Optional<RuleConfig> cfgOpt = ruleConfigRepository.findByRuleCode(rule.code());
            if (cfgOpt.isEmpty() || !cfgOpt.get().isEnabled()) {
                continue;
            }
            try {
                rule.evaluate(ctx, cfgOpt.get(), trigger)
                        .flatMap(alertPersister::persistIfNew)
                        .ifPresent(created::add);
            } catch (Exception e) {
                log.error("Rule {} failed for txn {}", rule.code(), trigger.getId(), e);
            }
        }
        return created;
    }
}
