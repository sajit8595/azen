package com.meridiantrust.sentinel.detection.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridiantrust.sentinel.detection.DetectionContext;
import com.meridiantrust.sentinel.detection.DetectionRule;
import com.meridiantrust.sentinel.detection.RuleResult;
import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.domain.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business Rule 2: 3+ transactions from the same account within a 24h window,
 * each individually between 9,000 and 9,999 (INR), trigger a Structuring alert.
 */
@Component
public class StructuringRule implements DetectionRule {

    @Override
    public String code() {
        return "STRUCTURING";
    }

    @Override
    public Optional<RuleResult> evaluate(DetectionContext ctx, RuleConfig config, Transaction t) {
        JsonNode p = ctx.params(config);
        BigDecimal lower = ctx.decimalParam(p, "lowerInr", new BigDecimal("9000"));
        BigDecimal upper = ctx.decimalParam(p, "upperInr", new BigDecimal("9999"));
        int minCount = ctx.intParam(p, "minCount", 3);
        int windowHours = ctx.intParam(p, "windowHours", 24);

        if (!inBand(t.getAmountInr(), lower, upper)) {
            return Optional.empty();
        }
        LocalDateTime to = t.getTxnTimestamp();
        LocalDateTime from = to.minusHours(windowHours);
        List<Transaction> band = ctx.windowFor(t.getAccountId(), from, to).stream()
                .filter(x -> inBand(x.getAmountInr(), lower, upper))
                .toList();

        if (band.size() < minCount) {
            return Optional.empty();
        }
        String customerId = ctx.customerIdFor(t.getAccountId());
        int score = Math.min(100, config.getBaseScore() + (band.size() - minCount) * 3);
        String explanation = String.format(
                "Account %s made %d transactions between INR %,.2f and %,.2f within %dh — "
                        + "a classic structuring/smurfing pattern to stay under reporting thresholds.",
                t.getAccountId(), band.size(), lower, upper, windowHours);
        // Dedup per account + 24h day bucket so the same burst isn't re-alerted per txn.
        String dayBucket = to.toLocalDate().toString();
        String dedup = "STRUCTURING|" + t.getAccountId() + "|" + dayBucket;
        return Optional.of(new RuleResult(code(), config.getRuleName(), score, explanation,
                RuleResult.ids(band), customerId, t.getAccountId(), dedup));
    }

    private boolean inBand(BigDecimal amt, BigDecimal lower, BigDecimal upper) {
        return amt.compareTo(lower) >= 0 && amt.compareTo(upper) <= 0;
    }
}
