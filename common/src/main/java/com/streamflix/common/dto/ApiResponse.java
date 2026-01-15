package com.streamflix.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standardized API response wrapper.
 *
 * All Streamflix APIs return responses in this format for consistency.
 * This enables:
 * - Uniform error handling across clients
 * - Consistent pagination metadata
 * - Request tracing via correlation IDs
 *
 * @param <T> The type of data contained in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Whether the request was successful.
     */
    private boolean success;

    /**
     * The response payload.
     */
    private T data;

    /**
     * Error information if success is false.
     */
    private ErrorInfo error;

    /**
     * Pagination metadata for list responses.
     */
    private PageInfo pagination;

    /**
     * Timestamp when the response was generated.
     */
    private Instant timestamp;

    /**
     * Correlation ID for request tracing.
     */
    private String correlationId;

    /**
     * API version that generated this response.
     */
    private String apiVersion;

    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a successful response with data and correlation ID.
     */
    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .correlationId(correlationId)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create a successful paginated response.
     */
    public static <T> ApiResponse<T> success(T data, PageInfo pagination) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .pagination(pagination)
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    /**
     * Create an error response with details.
     */
    public static <T> ApiResponse<T> error(String code, String message, List<FieldError> fieldErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorInfo.builder()
                        .code(code)
                        .message(message)
                        .fieldErrors(fieldErrors)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorInfo {
        /**
         * Machine-readable error code.
         */
        private String code;

        /**
         * Human-readable error message.
         */
        private String message;

        /**
         * Detailed field-level validation errors.
         */
        private List<FieldError> fieldErrors;

        /**
         * Link to documentation about this error.
         */
        private String documentationUrl;

        /**
         * Trace ID for support escalation.
         */
        private String traceId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public static PageInfo of(int page, int size, long totalElements) {
            int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
            return PageInfo.builder()
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .build();
        }
    }
}
