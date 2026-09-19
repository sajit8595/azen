package com.meridiantrust.sentinel.detection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meridiantrust.sentinel.domain.Account;
import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.domain.Transaction;
import com.meridiantrust.sentinel.repository.AccountRepository;
import com.meridiantrust.sentinel.repository.HighRiskJurisdictionRepository;
import com.meridiantrust.sentinel.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Provides rules with access to account/customer resolution, recent
 * transaction history, high-risk jurisdiction lookups, and config param parsing.
 */
public class DetectionContext {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final HighRiskJurisdictionRepository highRiskRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public DetectionContext(AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            HighRiskJurisdictionRepository highRiskRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.highRiskRepository = highRiskRepository;
    }

    public Optional<Account> account(String accountId) {
        return accountRepository.findById(accountId);
    }

    public String customerIdFor(String accountId) {
        return account(accountId).map(Account::getCustomerId).orElse(null);
    }

    public List<Transaction> windowFor(String accountId, LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findInWindow(accountId, from, to);
    }

    public boolean isHighRisk(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) return false;
        return highRiskRepository.findByCountryCodeAndActiveTrue(countryCode.trim().toUpperCase())
                .isPresent();
    }

    /** Parse rule_config.params_json into a JsonNode for threshold lookups. */
    public JsonNode params(RuleConfig config) {
        try {
            String json = config.getParamsJson();
            return mapper.readTree(json == null || json.isBlank() ? "{}" : json);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid params_json for rule " + config.getRuleCode(), e);
        }
    }

    public BigDecimal decimalParam(JsonNode node, String field, BigDecimal def) {
        return node.has(field) ? new BigDecimal(node.get(field).asText()) : def;
    }

    public int intParam(JsonNode node, String field, int def) {
        return node.has(field) ? node.get(field).asInt() : def;
    }

    public double doubleParam(JsonNode node, String field, double def) {
        return node.has(field) ? node.get(field).asDouble() : def;
    }
}
