package com.streamflix.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamflix.common.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;

/**
 * Global exception handler for the API Gateway.
 *
 * Translates exceptions into consistent API responses.
 * Handles:
 * - Circuit breaker open (503)
 * - Service unavailable (503)
 * - Timeout errors (504)
 * - Not found (404)
 * - All other errors (500)
 */
@Slf4j
@Order(-1)  // Run before default handler
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        String correlationId = exchange.getAttribute("correlationId");
        String path = exchange.getRequest().getPath().value();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status;
        String errorCode;
        String message;

        if (ex instanceof CallNotPermittedException) {
            // Circuit breaker is open
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = "CIRCUIT_BREAKER_OPEN";
            message = "Service temporarily unavailable. Please try again later.";
            log.warn("[{}] Circuit breaker open for path: {}", correlationId, path);

        } else if (ex instanceof TimeoutException || ex instanceof java.util.concurrent.TimeoutException) {
            // Request timeout
            status = HttpStatus.GATEWAY_TIMEOUT;
            errorCode = "GATEWAY_TIMEOUT";
            message = "Request timed out. Please try again.";
            log.warn("[{}] Timeout for path: {}", correlationId, path);

        } else if (ex instanceof ConnectException) {
            // Cannot connect to downstream service
            status = HttpStatus.SERVICE_UNAVAILABLE;
            errorCode = "SERVICE_UNAVAILABLE";
            message = "Service is currently unavailable. Please try again later.";
            log.error("[{}] Connection failed for path: {} - {}", correlationId, path, ex.getMessage());

        } else if (ex instanceof NotFoundException) {
            // Route not found
            status = HttpStatus.NOT_FOUND;
            errorCode = "ROUTE_NOT_FOUND";
            message = "The requested resource was not found.";
            log.debug("[{}] Route not found: {}", correlationId, path);

        } else if (ex instanceof ResponseStatusException rse) {
            // Response status exception
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            errorCode = "REQUEST_ERROR";
            message = rse.getReason() != null ? rse.getReason() : "Request error";
            log.debug("[{}] ResponseStatusException for path: {} - {}", correlationId, path, message);

        } else {
            // Unknown error
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "INTERNAL_ERROR";
            message = "An unexpected error occurred. Please try again later.";
            log.error("[{}] Unhandled exception for path: {}", correlationId, path, ex);
        }

        return writeErrorResponse(response, status, errorCode, message, correlationId);
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, HttpStatus status,
                                           String errorCode, String message, String correlationId) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(false)
                .error(ApiResponse.ErrorInfo.builder()
                        .code(errorCode)
                        .message(message)
                        .traceId(correlationId)
                        .build())
                .correlationId(correlationId)
                .timestamp(java.time.Instant.now())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(apiResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            // Fallback to simple JSON
            String json = String.format(
                    "{\"success\":false,\"error\":{\"code\":\"%s\",\"message\":\"%s\"}}",
                    errorCode, message
            );
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        }
    }
}
