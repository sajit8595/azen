package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.domain.Alert;
import com.meridiantrust.sentinel.dto.Dtos.AlertStats;
import com.meridiantrust.sentinel.dto.Dtos.AlertView;
import com.meridiantrust.sentinel.repository.AlertRepository;
import com.meridiantrust.sentinel.web.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /** Alert queue, filterable by status/min score, sorted (default riskScore desc via Pageable). */
    public Page<AlertView> queue(String status, Integer minRiskScore, Pageable pageable) {
        Page<Alert> page;
        if (status != null && !status.isBlank()) {
            page = alertRepository.findByStatus(Alert.Status.valueOf(status.toUpperCase()), pageable);
        } else if (minRiskScore != null) {
            page = alertRepository.findByRiskScoreGreaterThanEqual(minRiskScore, pageable);
        } else {
            page = alertRepository.findAll(pageable);
        }
        return page.map(AlertView::from);
    }

    public AlertView get(Long id) {
        return AlertView.from(alertRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Alert", id)));
    }

    public AlertStats stats() {
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Alert.Severity s : Alert.Severity.values()) {
            bySeverity.put(s.name(), alertRepository.countBySeverity(s));
        }
        Map<String, Long> byRule = new LinkedHashMap<>();
        alertRepository.findAll().forEach(a ->
                byRule.merge(a.getRuleCode(), 1L, Long::sum));
        return new AlertStats(
                alertRepository.countByStatus(Alert.Status.OPEN),
                alertRepository.countByStatus(Alert.Status.IN_REVIEW),
                alertRepository.countByStatus(Alert.Status.CLOSED),
                bySeverity, byRule);
    }
}
