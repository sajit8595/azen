package com.meridiantrust.sentinel.dto;

import com.meridiantrust.sentinel.domain.Alert;
import com.meridiantrust.sentinel.domain.AmlCase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Request/response DTOs for the API. */
public class Dtos {

    // ----- Auth -----
    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String role, String username) {}

    // ----- Customer (PII masked in list, full in detail) -----
    public record CustomerSummary(
            String id, String maskedName, String city, String state,
            String riskRating, String customerSegment, boolean politicallyExposed) {}

    public record CustomerDetail(
            String id, String firstName, String lastName, String email,
            String phoneNumber, String city, String state, String country,
            String occupation, BigDecimal annualIncome, String customerSegment,
            String kycStatus, String riskRating, boolean politicallyExposed) {}

    // ----- Transaction -----
    public record TransactionView(
            String id, String accountId, String direction, BigDecimal amount,
            String currency, BigDecimal amountInr, String counterpartyName,
            String counterpartyCountry, String channel, LocalDateTime txnTimestamp) {}

    public record TransactionRequest(
            String transactionId, String accountId, String direction, BigDecimal amount,
            String currency, String counterpartyName, String counterpartyAccount,
            String counterpartyCountry, String channel, LocalDateTime txnTimestamp) {}

    public record StreamingResult(TransactionView transaction, List<AlertView> alerts) {}

    // ----- Alert -----
    public record AlertView(
            Long id, String customerId, String accountId, String ruleCode, String ruleName,
            int riskScore, String severity, String status, String explanation,
            List<String> evidenceTxnIds, LocalDateTime createdAt) {
        public static AlertView from(Alert a) {
            List<String> evidence = (a.getEvidenceTxnIds() == null || a.getEvidenceTxnIds().isBlank())
                    ? List.of() : List.of(a.getEvidenceTxnIds().split(","));
            return new AlertView(a.getId(), a.getCustomerId(), a.getAccountId(), a.getRuleCode(),
                    a.getRuleName(), a.getRiskScore(), a.getSeverity().name(), a.getStatus().name(),
                    a.getExplanation(), evidence, a.getCreatedAt());
        }
    }

    public record AlertStats(long open, long inReview, long closed,
                             java.util.Map<String, Long> bySeverity,
                             java.util.Map<String, Long> byRule) {}

    // ----- Case -----
    public record CreateCaseRequest(Long alertId) {}
    public record DispositionRequest(String disposition, String reason) {}

    public record CaseView(
            Long id, Long alertId, String status, String assignedTo,
            String disposition, String dispositionReason, String analystId,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static CaseView from(AmlCase c) {
            return new CaseView(c.getId(), c.getAlertId(), c.getStatus().name(), c.getAssignedTo(),
                    c.getDisposition() == null ? null : c.getDisposition().name(),
                    c.getDispositionReason(), c.getAnalystId(), c.getCreatedAt(), c.getUpdatedAt());
        }
    }

    // ----- Rule config (admin) -----
    public record RuleConfigView(Long id, String ruleCode, String ruleName,
                                 boolean enabled, int baseScore, String paramsJson) {}
    public record RuleConfigUpdate(Boolean enabled, Integer baseScore, String paramsJson) {}
}
