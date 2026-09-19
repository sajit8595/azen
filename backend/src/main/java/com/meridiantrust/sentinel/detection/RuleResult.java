package com.meridiantrust.sentinel.detection;

import com.meridiantrust.sentinel.domain.Transaction;

import java.util.List;

/**
 * Output of a detection rule when it fires: the rule identity, a computed risk
 * score (0-100), a human-readable explanation, and the supporting evidence.
 */
public record RuleResult(
        String ruleCode,
        String ruleName,
        int riskScore,
        String explanation,
        List<String> evidenceTxnIds,
        String customerId,
        String accountId,
        String dedupKey
) {
    public static List<String> ids(List<Transaction> txns) {
        return txns.stream().map(Transaction::getId).toList();
    }
}
