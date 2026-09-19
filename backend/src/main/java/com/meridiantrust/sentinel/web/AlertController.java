package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.dto.Dtos.AlertStats;
import com.meridiantrust.sentinel.dto.Dtos.AlertView;
import com.meridiantrust.sentinel.service.AlertService;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /** Alert queue. Defaults to risk score descending so the riskiest sort to the top. */
    @GetMapping
    public ApiResponse<Page<AlertView>> queue(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(required = false) Integer minRiskScore) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "riskScore"));
        return ApiResponse.ok(alertService.queue(status, minRiskScore, pageable));
    }

    @GetMapping("/stats")
    public ApiResponse<AlertStats> stats() {
        return ApiResponse.ok(alertService.stats());
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertView> get(@PathVariable Long id) {
        return ApiResponse.ok(alertService.get(id), String.valueOf(id));
    }
}
