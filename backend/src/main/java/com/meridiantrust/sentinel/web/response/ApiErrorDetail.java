package com.meridiantrust.sentinel.web.response;

/** A single error entry: a machine-readable code and a human-readable message. */
public record ApiErrorDetail(String code, String message) {
}
