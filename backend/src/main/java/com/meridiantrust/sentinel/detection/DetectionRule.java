package com.meridiantrust.sentinel.detection;

import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.domain.Transaction;

import java.util.Optional;

/**
 * A single AML detection rule. Rules are stateless and evaluate an incoming
 * transaction against recent history for the same account/customer.
 * Configuration (thresholds, windows) is passed in from the rule_config table
 * so rules can be tuned without redeployment.
 */
public interface DetectionRule {

    /** Stable rule code, matches rule_config.rule_code. */
    String code();

    /**
     * Evaluate the trigger transaction. Returns a RuleResult when the rule
     * fires, otherwise empty.
     */
    Optional<RuleResult> evaluate(DetectionContext ctx, RuleConfig config, Transaction trigger);
}
