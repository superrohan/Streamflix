package com.streamflix.common.util;

import java.util.UUID;

/**
 * Generates correlation IDs for distributed tracing.
 *
 * Correlation IDs are used to track requests across all services,
 * enabling end-to-end debugging and log aggregation.
 *
 * Format: prefix-timestamp-uuid
 * Example: stfx-1704067200000-a1b2c3d4
 */
public final class CorrelationIdGenerator {

    private static final String PREFIX = "stfx";

    private CorrelationIdGenerator() {
        // Prevent instantiation
    }

    /**
     * Generate a new correlation ID.
     */
    public static String generate() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s-%d-%s", PREFIX, System.currentTimeMillis(), uuid);
    }

    /**
     * Validate a correlation ID format.
     */
    public static boolean isValid(String correlationId) {
        if (correlationId == null || correlationId.isEmpty()) {
            return false;
        }
        // Basic validation: starts with prefix and has expected structure
        return correlationId.startsWith(PREFIX + "-") && correlationId.length() >= 20;
    }

    /**
     * Get or generate a correlation ID.
     * If the provided ID is null or invalid, generate a new one.
     */
    public static String getOrGenerate(String correlationId) {
        return isValid(correlationId) ? correlationId : generate();
    }
}
