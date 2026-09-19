package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.dto.Dtos.TransactionView;
import com.meridiantrust.sentinel.repository.AccountRepository;
import com.meridiantrust.sentinel.repository.TransactionRepository;
import com.meridiantrust.sentinel.web.error.ResourceNotFoundException;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountController(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/{id}/transactions")
    public ApiResponse<List<TransactionView>> transactions(@PathVariable String id) {
        if (!accountRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Account", id);
        }
        List<TransactionView> views = transactionRepository.findByAccountIdOrderByTxnTimestampDesc(id).stream()
                .map(t -> new TransactionView(t.getId(), t.getAccountId(), t.getDirection().name(),
                        t.getAmount(), t.getCurrency(), t.getAmountInr(), t.getCounterpartyName(),
                        t.getCounterpartyCountry(), t.getChannel(), t.getTxnTimestamp()))
                .toList();
        return ApiResponse.ok(views, id);
    }
}
