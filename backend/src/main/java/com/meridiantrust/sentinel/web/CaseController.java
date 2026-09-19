package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.dto.Dtos.CaseView;
import com.meridiantrust.sentinel.dto.Dtos.CreateCaseRequest;
import com.meridiantrust.sentinel.dto.Dtos.DispositionRequest;
import com.meridiantrust.sentinel.service.CaseService;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CaseView>> create(@RequestBody CreateCaseRequest request, Authentication auth) {
        CaseView view = caseService.createCase(request.alertId(), actor(auth));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(view, String.valueOf(view.id())));
    }

    @GetMapping
    public ApiResponse<List<CaseView>> list(@RequestParam(required = false) String status) {
        return ApiResponse.ok(caseService.list(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<CaseView> get(@PathVariable Long id) {
        return ApiResponse.ok(caseService.get(id), String.valueOf(id));
    }

    @PatchMapping("/{id}/disposition")
    public ApiResponse<CaseView> disposition(@PathVariable Long id,
                                             @RequestBody DispositionRequest request,
                                             Authentication auth) {
        CaseView view = caseService.disposition(id, request, actor(auth));
        return ApiResponse.ok(view, String.valueOf(id));
    }

    private String actor(Authentication auth) {
        return auth != null ? auth.getName() : "UNKNOWN";
    }
}
