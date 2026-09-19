package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.domain.Account;
import com.meridiantrust.sentinel.domain.Customer;
import com.meridiantrust.sentinel.domain.Transaction;
import com.meridiantrust.sentinel.dto.IngestResult;
import com.meridiantrust.sentinel.repository.AccountRepository;
import com.meridiantrust.sentinel.repository.CustomerRepository;
import com.meridiantrust.sentinel.repository.TransactionRepository;
import com.opencsv.CSVReaderHeaderAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Parses uploaded CSV files, validates rows (malformed data + referential
 * integrity), normalizes currency to INR, and persists. Ingestion errors are
 * logged and returned per-row rather than aborting the whole batch.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyService currencyService;

    public IngestionService(CustomerRepository customerRepository,
                            AccountRepository accountRepository,
                            TransactionRepository transactionRepository,
                            CurrencyService currencyService) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currencyService = currencyService;
    }

    @Transactional
    public IngestResult ingestCustomers(MultipartFile file) {
        IngestResult result = new IngestResult();
        int row = 1;
        try (Reader r = reader(file); CSVReaderHeaderAware csv = new CSVReaderHeaderAware(r)) {
            Map<String, String> line;
            while ((line = csv.readMap()) != null) {
                row++;
                try {
                    String id = req(line, "customer_id");
                    Customer c = new Customer();
                    c.setId(id);
                    c.setFirstName(line.get("first_name"));
                    c.setLastName(line.get("last_name"));
                    c.setGender(line.get("gender"));
                    c.setDateOfBirth(parseDate(line.get("date_of_birth")));
                    c.setEmail(line.get("email"));
                    c.setPhoneNumber(line.get("phone_number"));
                    c.setCity(line.get("city"));
                    c.setState(line.get("state"));
                    c.setCountry(line.get("country"));
                    c.setOccupation(line.get("occupation"));
                    c.setAnnualIncome(parseDecimal(line.get("annual_income")));
                    c.setCustomerSegment(line.get("customer_segment"));
                    c.setKycStatus(line.get("kyc_status"));
                    c.setRiskRating(line.get("risk_rating"));
                    c.setPoliticallyExposed("1".equals(line.get("is_politically_exposed"))
                            || "Y".equalsIgnoreCase(line.get("is_politically_exposed")));
                    c.setCustomerSince(parseDate(line.get("customer_since")));
                    customerRepository.save(c);
                    result.addInserted();
                } catch (Exception e) {
                    result.addFailure(row, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse customers CSV", e);
            result.addFailure(row, "Fatal parse error: " + e.getMessage());
        }
        log.info("Customer ingestion: {} inserted, {} failed", result.getInserted(), result.getFailed());
        return result;
    }

    @Transactional
    public IngestResult ingestAccounts(MultipartFile file) {
        IngestResult result = new IngestResult();
        int row = 1;
        try (Reader r = reader(file); CSVReaderHeaderAware csv = new CSVReaderHeaderAware(r)) {
            Map<String, String> line;
            while ((line = csv.readMap()) != null) {
                row++;
                try {
                    String id = req(line, "account_id");
                    String customerId = req(line, "customer_id");
                    if (!customerRepository.existsById(customerId)) {
                        throw new IllegalArgumentException("Unknown customer_id: " + customerId);
                    }
                    Account a = new Account();
                    a.setId(id);
                    a.setCustomerId(customerId);
                    a.setAccountType(line.get("account_type"));
                    a.setAccountStatus(line.get("account_status"));
                    a.setCurrency(orInr(line.get("currency")));
                    a.setOpenDate(parseDate(line.get("open_date")));
                    a.setCloseDate(parseDate(line.get("close_date")));
                    a.setBranchCode(line.get("branch_code"));
                    a.setBranchCity(line.get("branch_city"));
                    a.setCurrentBalance(parseDecimal(line.get("current_balance")));
                    a.setAccountTier(line.get("account_tier"));
                    accountRepository.save(a);
                    result.addInserted();
                } catch (Exception e) {
                    result.addFailure(row, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse accounts CSV", e);
            result.addFailure(row, "Fatal parse error: " + e.getMessage());
        }
        log.info("Account ingestion: {} inserted, {} failed", result.getInserted(), result.getFailed());
        return result;
    }

    /** Parses + persists transactions and returns the persisted list for detection. */
    @Transactional
    public IngestOutcome ingestTransactions(MultipartFile file) {
        IngestResult result = new IngestResult();
        IngestOutcome outcome = new IngestOutcome(result);
        int row = 1;
        try (Reader r = reader(file); CSVReaderHeaderAware csv = new CSVReaderHeaderAware(r)) {
            Map<String, String> line;
            while ((line = csv.readMap()) != null) {
                row++;
                try {
                    Transaction t = parseTransaction(line);
                    transactionRepository.save(t);
                    outcome.persisted.add(t);
                    result.addInserted();
                } catch (Exception e) {
                    result.addFailure(row, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse transactions CSV", e);
            result.addFailure(row, "Fatal parse error: " + e.getMessage());
        }
        log.info("Transaction ingestion: {} inserted, {} failed", result.getInserted(), result.getFailed());
        return outcome;
    }

    /** Builds and validates a single Transaction from a raw field map. */
    public Transaction parseTransaction(Map<String, String> line) {
        String id = req(line, "transaction_id");
        String accountId = req(line, "account_id");
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("Unknown account_id: " + accountId);
        }
        BigDecimal amount = parseDecimal(req(line, "amount"));
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        String currency = orInr(line.get("currency"));
        Transaction t = new Transaction();
        t.setId(id);
        t.setAccountId(accountId);
        t.setDirection(Transaction.Direction.valueOf(req(line, "direction").trim().toUpperCase()));
        t.setAmount(amount);
        t.setCurrency(currency);
        t.setAmountInr(currencyService.toInr(amount, currency));
        t.setCounterpartyName(line.get("counterparty_name"));
        t.setCounterpartyAccount(line.get("counterparty_account"));
        t.setCounterpartyCountry(line.get("counterparty_country"));
        t.setChannel(line.get("channel"));
        t.setTxnTimestamp(parseTimestamp(req(line, "txn_timestamp")));
        return t;
    }

    // ---------- helpers ----------
    private Reader reader(MultipartFile file) throws Exception {
        return new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
    }

    private static String req(Map<String, String> line, String key) {
        String v = line.get(key);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return v.trim();
    }

    private static String orInr(String c) {
        return (c == null || c.isBlank()) ? "INR" : c.trim().toUpperCase();
    }

    private static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDate.parse(s.trim());
    }

    private static LocalDateTime parseTimestamp(String s) {
        String v = s.trim();
        if (v.contains("T")) {
            return LocalDateTime.parse(v);
        }
        return LocalDateTime.parse(v, TS);
    }

    private static BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) return null;
        return new BigDecimal(s.trim());
    }

    /** Carries the ingestion result plus the persisted transactions for detection. */
    public static class IngestOutcome {
        public final IngestResult result;
        public final List<Transaction> persisted = new java.util.ArrayList<>();
        public IngestOutcome(IngestResult result) { this.result = result; }
    }
}
