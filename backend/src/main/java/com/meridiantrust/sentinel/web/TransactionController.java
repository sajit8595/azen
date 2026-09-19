package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.detection.DetectionEngine;
import com.meridiantrust.sentinel.domain.Alert;
import com.meridiantrust.sentinel.domain.Transaction;
import com.meridiantrust.sentinel.dto.Dtos.*;
import com.meridiantrust.sentinel.repository.AccountRepository;
import com.meridiantrust.sentinel.repository.TransactionRepository;
import com.meridiantrust.sentinel.service.CurrencyService;
import com.meridiantrust.sentinel.web.error.ResourceNotFoundException;
import com.meridiantrust.sentinel.web.error.ValidationException;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Streaming / incremental transaction ingestion: accept a single transaction as
 * JSON, persist it, and synchronously evaluate detection rules (sub-second path).
 */
@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CurrencyService currencyService;
    private final DetectionEngine detectionEngine;

    public TransactionController(TransactionRepository transactionRepository,
                                 AccountRepository accountRepository,
                                 CurrencyService currencyService,
                                 DetectionEngine detectionEngine) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.currencyService = currencyService;
        this.detectionEngine = detectionEngine;
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<StreamingResult>> ingestOne(@RequestBody TransactionRequest req) {
        if (!accountRepository.existsById(req.accountId())) {
            throw ResourceNotFoundException.of("Account", req.accountId());
        }
        if (req.amount() == null || req.amount().signum() <= 0) {
            throw new ValidationException("amount must be positive");
        }
        String currency = req.currency() == null ? "INR" : req.currency().toUpperCase();

        Transaction t = new Transaction();
        t.setId(req.transactionId());
        t.setAccountId(req.accountId());
        t.setDirection(Transaction.Direction.valueOf(req.direction().toUpperCase()));
        t.setAmount(req.amount());
        t.setCurrency(currency);
        t.setAmountInr(currencyService.toInr(req.amount(), currency));
        t.setCounterpartyName(req.counterpartyName());
        t.setCounterpartyAccount(req.counterpartyAccount());
        t.setCounterpartyCountry(req.counterpartyCountry());
        t.setChannel(req.channel());
        t.setTxnTimestamp(req.txnTimestamp());
        transactionRepository.save(t);

        List<Alert> alerts = detectionEngine.evaluate(t);
        StreamingResult result = new StreamingResult(
                new TransactionView(t.getId(), t.getAccountId(), t.getDirection().name(), t.getAmount(),
                        t.getCurrency(), t.getAmountInr(), t.getCounterpartyName(), t.getCounterpartyCountry(),
                        t.getChannel(), t.getTxnTimestamp()),
                alerts.stream().map(AlertView::from).toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result, t.getId()));
    }
}
