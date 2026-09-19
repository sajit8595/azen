package com.meridiantrust.sentinel.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Metadata attached to every response: the server timestamp and an optional
 * identifier (e.g. the id of a record just created or affected).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseMeta(String timestamp, String id) {

    public static ResponseMeta now(String id) {
        return new ResponseMeta(Instant.now().toString(), id);
    }
}
