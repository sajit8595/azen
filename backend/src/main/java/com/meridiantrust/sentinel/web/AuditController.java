package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.domain.AuditLog;
import com.meridiantrust.sentinel.service.AuditService;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ApiResponse<List<AuditLog>> trail(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId) {
        return ApiResponse.ok(auditService.trail(entityType, entityId));
    }
}
