package com.meridiantrust.sentinel.detection.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.meridiantrust.sentinel.detection.DetectionContext;
import com.meridiantrust.sentinel.detection.DetectionRule;
import com.meridiantrust.sentinel.detection.RuleResult;
import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.domain.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Business Rule 3: if funds are deposited (CREDIT) into an account and >= 80%
 * of that value is transferred out (DEBIT) within 48h, trigger a Rapid Movement
 * (layering) alert. Evaluated when a DEBIT arrives, looking back for the deposit.
 */
@Component
public class RapidMovementRule implements DetectionRule {

    @Override
    public String code() {
        return "RAPID_MOVEMENT";
    }

    @Override
    public Optional<RuleResult> evaluate(DetectionContext ctx, RuleConfig config, Transaction t) {
        if (t.getDirection() != Transaction.Direction.DEBIT) {
            return Optional.empty();
        }
        JsonNode p = ctx.params(config);
        double ratio = ctx.doubleParam(p, "outflowRatio", 0.8);
        int windowHours = ctx.intParam(p, "windowHours", 48);

        LocalDateTime to = t.getTxnTimestamp();
        LocalDateTime from = to.minusHours(windowHours);
        List<Transaction> window = ctx.windowFor(t.getAccountId(), from, to);

        // Largest single deposit inside the window.
        Transaction deposit = window.stream()
                .filter(x -> x.getDirection() == Transaction.Direction.CREDIT)
                .max((a, b) -> a.getAmountInr().compareTo(b.getAmountInr()))
                .orElse(null);
        if (deposit == null || deposit.getAmountInr().signum() <= 0) {
            return Optional.empty();
        }

        // Sum outflows that occurred after the deposit, within the window.
        List<Transaction> outflows = new ArrayList<>();
        BigDecimal outSum = BigDecimal.ZERO;
        for (Transaction x : window) {
            if (x.getDirection() == Transaction.Direction.DEBIT
                    && !x.getTxnTimestamp().isBefore(deposit.getTxnTimestamp())) {
                outflows.add(x);
                outSum = outSum.add(x.getAmountInr());
            }
        }
        BigDecimal threshold = deposit.getAmountInr()
                .multiply(BigDecimal.valueOf(ratio));
        if (outSum.compareTo(threshold) < 0) {
            return Optional.empty();
        }

        String customerId = ctx.customerIdFor(t.getAccountId());
        double pct = outSum.divide(deposit.getAmountInr(), 4, RoundingMode.HALF_UP).doubleValue() * 100;
        int score = Math.min(100, config.getBaseScore() + (int) Math.min(20, (pct - ratio * 100) / 2));
        String explanation = String.format(
                "Account %s received a deposit of INR %,.2f (txn %s) and moved out INR %,.2f (%.0f%%) "
                        + "within %dh — indicative of layering / rapid movement of funds.",
                t.getAccountId(), deposit.getAmountInr(), deposit.getId(), outSum, pct, windowHours);

        List<String> evidence = new ArrayList<>();
        evidence.add(deposit.getId());
        outflows.forEach(o -> evidence.add(o.getId()));
        String dedup = "RAPID_MOVEMENT|" + t.getAccountId() + "|" + deposit.getId();
        return Optional.of(new RuleResult(code(), config.getRuleName(), score, explanation,
                evidence, customerId, t.getAccountId(), dedup));
    }
}
