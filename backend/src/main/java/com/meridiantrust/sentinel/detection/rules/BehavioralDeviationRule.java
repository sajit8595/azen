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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business Rule 5: a customer's daily transaction value that exceeds
 * (multiplier x their N-day rolling daily average) triggers a Behavioral
 * Deviation alert. Evaluated at the account level using its transaction history
 * as the baseline (kept simple and deterministic for the hackathon scope).
 */
@Component
public class BehavioralDeviationRule implements DetectionRule {

    @Override
    public String code() {
        return "BEHAVIORAL_DEVIATION";
    }

    @Override
    public Optional<RuleResult> evaluate(DetectionContext ctx, RuleConfig config, Transaction t) {
        JsonNode p = ctx.params(config);
        double multiplier = ctx.doubleParam(p, "multiplier", 3.0);
        int baselineDays = ctx.intParam(p, "baselineDays", 90);

        LocalDate day = t.getTxnTimestamp().toLocalDate();
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.atTime(23, 59, 59);

        // Today's total value for the account.
        BigDecimal todayValue = ctx.windowFor(t.getAccountId(), dayStart, dayEnd).stream()
                .map(Transaction::getAmountInr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Baseline: average daily value over the preceding window (excluding today).
        LocalDateTime baselineStart = dayStart.minusDays(baselineDays);
        List<Transaction> baseline = ctx.windowFor(t.getAccountId(), baselineStart, dayStart.minusSeconds(1));
        if (baseline.isEmpty()) {
            return Optional.empty();
        }
        BigDecimal baselineTotal = baseline.stream()
                .map(Transaction::getAmountInr)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long distinctDays = baseline.stream()
                .map(x -> x.getTxnTimestamp().toLocalDate())
                .distinct().count();
        if (distinctDays == 0) {
            return Optional.empty();
        }
        BigDecimal avgDaily = baselineTotal.divide(BigDecimal.valueOf(distinctDays), 2, RoundingMode.HALF_UP);
        if (avgDaily.signum() <= 0) {
            return Optional.empty();
        }
        BigDecimal threshold = avgDaily.multiply(BigDecimal.valueOf(multiplier));
        if (todayValue.compareTo(threshold) <= 0) {
            return Optional.empty();
        }

        String customerId = ctx.customerIdFor(t.getAccountId());
        double factor = todayValue.divide(avgDaily, 2, RoundingMode.HALF_UP).doubleValue();
        int score = Math.min(100, config.getBaseScore() + (int) Math.min(25, (factor - multiplier) * 3));
        String explanation = String.format(
                "Account %s transacted INR %,.2f on %s — %.1fx its %d-day average daily value of INR %,.2f "
                        + "(threshold %.0fx). Significant behavioral deviation.",
                t.getAccountId(), todayValue, day, factor, baselineDays, avgDaily, multiplier);

        List<String> evidence = ctx.windowFor(t.getAccountId(), dayStart, dayEnd)
                .stream().map(Transaction::getId).toList();
        String dedup = "BEHAVIORAL_DEVIATION|" + t.getAccountId() + "|" + day;
        return Optional.of(new RuleResult(code(), config.getRuleName(), score, explanation,
                evidence, customerId, t.getAccountId(), dedup));
    }
}
