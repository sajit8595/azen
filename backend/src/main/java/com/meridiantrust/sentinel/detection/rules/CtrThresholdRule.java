package com.meridiantrust.sentinel.detection.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridiantrust.sentinel.detection.DetectionContext;
import com.meridiantrust.sentinel.detection.DetectionRule;
import com.meridiantrust.sentinel.detection.RuleResult;
import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.domain.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Business Rule 1: any single transaction >= threshold (default 10,000 INR)
 * is flagged for review (CTR-style).
 */
@Component
public class CtrThresholdRule implements DetectionRule {

    @Override
    public String code() {
        return "CTR_THRESHOLD";
    }

    @Override
    public Optional<RuleResult> evaluate(DetectionContext ctx, RuleConfig config, Transaction t) {
        JsonNode p = ctx.params(config);
        BigDecimal threshold = ctx.decimalParam(p, "thresholdInr", new BigDecimal("10000"));
        if (t.getAmountInr().compareTo(threshold) < 0) {
            return Optional.empty();
        }
        String customerId = ctx.customerIdFor(t.getAccountId());
        // Score scales up with how far it exceeds the threshold, capped at 100.
        int score = Math.min(100, config.getBaseScore()
                + (int) Math.min(30, t.getAmountInr().subtract(threshold)
                    .divide(threshold, java.math.RoundingMode.DOWN).intValue() * 5));
        String explanation = String.format(
                "Transaction %s of INR %,.2f meets/exceeds the CTR reporting threshold of INR %,.2f.",
                t.getId(), t.getAmountInr(), threshold);
        String dedup = "CTR_THRESHOLD|" + t.getId();
        return Optional.of(new RuleResult(code(), config.getRuleName(), score, explanation,
                List.of(t.getId()), customerId, t.getAccountId(), dedup));
    }
}
