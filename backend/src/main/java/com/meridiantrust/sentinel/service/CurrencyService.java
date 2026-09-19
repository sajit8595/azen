package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Normalizes monetary amounts to the base currency (INR) using the
 * configurable exchange_rate table (Business Rule 9).
 */
@Service
public class CurrencyService {

    private final ExchangeRateRepository exchangeRateRepository;

    public CurrencyService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public BigDecimal toInr(BigDecimal amount, String currency) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        String cur = currency == null ? "INR" : currency.trim().toUpperCase();
        if (cur.equals("INR")) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rate = exchangeRateRepository.findByCurrency(cur)
                .map(r -> r.getRateToInr())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No exchange rate configured for currency: " + cur));
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
