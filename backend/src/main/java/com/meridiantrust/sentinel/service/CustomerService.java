package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.domain.Customer;
import com.meridiantrust.sentinel.domain.Transaction;
import com.meridiantrust.sentinel.dto.Dtos.CustomerDetail;
import com.meridiantrust.sentinel.dto.Dtos.CustomerSummary;
import com.meridiantrust.sentinel.dto.Dtos.TransactionView;
import com.meridiantrust.sentinel.repository.CustomerRepository;
import com.meridiantrust.sentinel.repository.TransactionRepository;
import com.meridiantrust.sentinel.web.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public CustomerService(CustomerRepository customerRepository, TransactionRepository transactionRepository) {
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    /** List view with masked PII (Business Rule 8). */
    public Page<CustomerSummary> list(String riskRating, Pageable pageable) {
        Page<Customer> page = (riskRating == null || riskRating.isBlank())
                ? customerRepository.findAll(pageable)
                : customerRepository.findByRiskRating(riskRating.toUpperCase(), pageable);
        return page.map(this::toSummary);
    }

    /** Full detail (authorized roles only; PII visible). */
    public CustomerDetail detail(String id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
        return new CustomerDetail(c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(),
                c.getPhoneNumber(), c.getCity(), c.getState(), c.getCountry(), c.getOccupation(),
                c.getAnnualIncome(), c.getCustomerSegment(), c.getKycStatus(), c.getRiskRating(),
                c.isPoliticallyExposed());
    }

    public List<TransactionView> transactions(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw ResourceNotFoundException.of("Customer", customerId);
        }
        return transactionRepository.findByCustomerId(customerId).stream().map(this::toView).toList();
    }

    private CustomerSummary toSummary(Customer c) {
        return new CustomerSummary(c.getId(), maskName(c.getFirstName(), c.getLastName()),
                c.getCity(), c.getState(), c.getRiskRating(), c.getCustomerSegment(),
                c.isPoliticallyExposed());
    }

    private TransactionView toView(Transaction t) {
        return new TransactionView(t.getId(), t.getAccountId(), t.getDirection().name(), t.getAmount(),
                t.getCurrency(), t.getAmountInr(), t.getCounterpartyName(), t.getCounterpartyCountry(),
                t.getChannel(), t.getTxnTimestamp());
    }

    /** Masks a name to first initial + asterisks, e.g. "Krishna Sharma" -> "K***** S*****". */
    static String maskName(String first, String last) {
        return mask(first) + " " + mask(last);
    }

    private static String mask(String s) {
        if (s == null || s.isBlank()) return "";
        if (s.length() == 1) return s;
        return s.charAt(0) + "*".repeat(Math.max(1, s.length() - 1));
    }
}
