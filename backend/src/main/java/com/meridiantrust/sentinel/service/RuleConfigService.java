package com.meridiantrust.sentinel.service;

import com.meridiantrust.sentinel.domain.RuleConfig;
import com.meridiantrust.sentinel.dto.Dtos.RuleConfigUpdate;
import com.meridiantrust.sentinel.dto.Dtos.RuleConfigView;
import com.meridiantrust.sentinel.repository.RuleConfigRepository;
import com.meridiantrust.sentinel.web.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Lets ADMIN tune detection rule thresholds without redeploying. */
@Service
public class RuleConfigService {

    private final RuleConfigRepository repository;

    public RuleConfigService(RuleConfigRepository repository) {
        this.repository = repository;
    }

    public List<RuleConfigView> list() {
        return repository.findAll().stream().map(this::toView).toList();
    }

    @Transactional
    public RuleConfigView update(String ruleCode, RuleConfigUpdate update) {
        RuleConfig cfg = repository.findByRuleCode(ruleCode)
                .orElseThrow(() -> ResourceNotFoundException.of("Rule", ruleCode));
        if (update.enabled() != null) cfg.setEnabled(update.enabled());
        if (update.baseScore() != null) cfg.setBaseScore(update.baseScore());
        if (update.paramsJson() != null && !update.paramsJson().isBlank()) {
            cfg.setParamsJson(update.paramsJson());
        }
        cfg.setUpdatedAt(LocalDateTime.now());
        return toView(repository.save(cfg));
    }

    private RuleConfigView toView(RuleConfig c) {
        return new RuleConfigView(c.getId(), c.getRuleCode(), c.getRuleName(),
                c.isEnabled(), c.getBaseScore(), c.getParamsJson());
    }
}
