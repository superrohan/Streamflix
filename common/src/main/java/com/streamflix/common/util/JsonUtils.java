package com.streamflix.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * JSON serialization utilities.
 *
 * Provides a pre-configured ObjectMapper and convenience methods
 * for JSON operations throughout the platform.
 */
@Slf4j
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private JsonUtils() {
        // Prevent instantiation
    }

    /**
     * Get the shared ObjectMapper instance.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Serialize an object to JSON string.
     */
    public static String toJson(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Serialize an object to pretty-printed JSON string.
     */
    public static String toPrettyJson(Object object) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Deserialize JSON string to object.
     */
    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}", type.getSimpleName(), e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Deserialize JSON string to object with type reference.
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON", e);
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Safely deserialize JSON, returning empty Optional on failure.
     */
    public static <T> Optional<T> fromJsonSafe(String json, Class<T> type) {
        try {
            return Optional.ofNullable(OBJECT_MAPPER.readValue(json, type));
        } catch (Exception e) {
            log.warn("Failed to deserialize JSON to {}: {}", type.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Convert object to another type via JSON (deep copy/conversion).
     */
    public static <T> T convert(Object source, Class<T> targetType) {
        return OBJECT_MAPPER.convertValue(source, targetType);
    }
}
