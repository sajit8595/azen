package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.dto.Dtos.RuleConfigUpdate;
import com.meridiantrust.sentinel.dto.Dtos.RuleConfigView;
import com.meridiantrust.sentinel.service.RuleConfigService;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RuleConfigService ruleConfigService;

    public AdminController(RuleConfigService ruleConfigService) {
        this.ruleConfigService = ruleConfigService;
    }

    @GetMapping("/rules")
    public ApiResponse<List<RuleConfigView>> rules() {
        return ApiResponse.ok(ruleConfigService.list());
    }

    @PatchMapping("/rules/{code}")
    public ApiResponse<RuleConfigView> updateRule(@PathVariable String code, @RequestBody RuleConfigUpdate update) {
        return ApiResponse.ok(ruleConfigService.update(code, update), code);
    }
}
