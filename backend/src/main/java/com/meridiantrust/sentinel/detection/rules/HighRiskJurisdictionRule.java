package com.meridiantrust.sentinel.detection.rules;

import com.meridiantrust.sentinel.detection.DetectionContext;
import com.meridiantrust.sentinel.detection.DetectionRule;
import com.meridiantrust.sentinel.detection.RuleResult;
import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Business Rule 4: any transaction whose counterparty country is on the
 * configurable high-risk / sanctions list generates an alert regardless of amount.
 */
@Component
public class HighRiskJurisdictionRule implements DetectionRule {

    @Override
    public String code() {
        return "HIGH_RISK_JURISDICTION";
    }

    @Override
    public Optional<RuleResult> evaluate(DetectionContext ctx, RuleConfig config, Transaction t) {
        if (!ctx.isHighRisk(t.getCounterpartyCountry())) {
            return Optional.empty();
        }
        String customerId = ctx.customerIdFor(t.getAccountId());
        String explanation = String.format(
                "Transaction %s involves counterparty '%s' in high-risk jurisdiction '%s' "
                        + "(INR %,.2f) — flagged regardless of amount.",
                t.getId(), t.getCounterpartyName(), t.getCounterpartyCountry(), t.getAmountInr());
        String dedup = "HIGH_RISK_JURISDICTION|" + t.getId();
        return Optional.of(new RuleResult(code(), config.getRuleName(), config.getBaseScore(),
                explanation, List.of(t.getId()), customerId, t.getAccountId(), dedup));
    }
}
