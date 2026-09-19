package com.meridiantrust.sentinel.dto;

import java.util.ArrayList;
import java.util.List;

/** Result of a CSV ingestion run. */
public class IngestResult {
    private int inserted;
    private int failed;
    private final List<String> errors = new ArrayList<>();

    public void addInserted() { this.inserted++; }
    public void addFailure(int rowNum, String message) {
        this.failed++;
        this.errors.add("Row " + rowNum + ": " + message);
    }

    public int getInserted() { return inserted; }
    public int getFailed() { return failed; }
    public List<String> getErrors() { return errors; }
}
