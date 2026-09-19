package com.meridiantrust.sentinel.web;

import com.meridiantrust.sentinel.detection.DetectionEngine;
import com.meridiantrust.sentinel.dto.IngestResult;
import com.meridiantrust.sentinel.service.IngestionService;
import com.meridiantrust.sentinel.web.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final IngestionService ingestionService;
    private final DetectionEngine detectionEngine;

    public IngestController(IngestionService ingestionService, DetectionEngine detectionEngine) {
        this.ingestionService = ingestionService;
        this.detectionEngine = detectionEngine;
    }

    @PostMapping("/customers")
    public ApiResponse<IngestResult> customers(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(ingestionService.ingestCustomers(file));
    }

    @PostMapping("/accounts")
    public ApiResponse<IngestResult> accounts(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(ingestionService.ingestAccounts(file));
    }

    /** Ingests transactions then runs the detection engine over them. */
    @PostMapping("/transactions")
    public ApiResponse<IngestResult> transactions(@RequestParam("file") MultipartFile file) {
        IngestionService.IngestOutcome outcome = ingestionService.ingestTransactions(file);
        detectionEngine.evaluateBatch(outcome.persisted);
        return ApiResponse.ok(outcome.result);
    }
}
