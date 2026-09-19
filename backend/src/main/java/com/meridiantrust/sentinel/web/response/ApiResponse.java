package com.meridiantrust.sentinel.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Uniform response envelope returned by every endpoint, for both success and
 * error outcomes so the frontend always parses the same shape.
 *
 * <ul>
 *   <li>Success: {@code data} is populated, {@code errors} is null/omitted.</li>
 *   <li>Error: {@code data} is null (and omitted from JSON), {@code errors} is populated.</li>
 * </ul>
 *
 * {@code data} is annotated NON_NULL so it is not serialized when null on errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        ResponseMeta metaData,
        List<ApiErrorDetail> errors) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, ResponseMeta.now(null), null);
    }

    public static <T> ApiResponse<T> ok(T data, String id) {
        return new ApiResponse<>(data, ResponseMeta.now(id), null);
    }

    public static <T> ApiResponse<T> error(List<ApiErrorDetail> errors) {
        return new ApiResponse<>(null, ResponseMeta.now(null), errors);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return error(List.of(new ApiErrorDetail(code, message)));
    }
}
