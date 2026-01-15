package com.streamflix.auth.service;

import com.streamflix.auth.entity.Profile;
import com.streamflix.auth.entity.User;
import com.streamflix.common.event.UserEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Publishes user-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String SOURCE = "auth-service";

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "publishEventFallback")
    @Retry(name = "kafkaProducer")
    public void publishRegistrationEvent(User user) {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_REGISTERED")
                .timestamp(Instant.now())
                .source(SOURCE)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .userEventType(UserEvent.UserEventType.USER_REGISTERED)
                .version(1)
                .build();

        sendEvent(event);
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "publishEventFallback")
    @Retry(name = "kafkaProducer")
    public void publishLoginEvent(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .toList();

        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("USER_LOGIN")
                .timestamp(Instant.now())
                .source(SOURCE)
                .userId(user.getId().toString())
                .email(user.getEmail())
                .userEventType(UserEvent.UserEventType.USER_LOGIN)
                .roles(roles)
                .version(1)
                .build();

        sendEvent(event);
    }

    @CircuitBreaker(name = "kafkaProducer", fallbackMethod = "publishEventFallback")
    @Retry(name = "kafkaProducer")
    public void publishProfileCreatedEvent(User user, Profile profile) {
        UserEvent event = UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PROFILE_CREATED")
                .timestamp(Instant.now())
                .source(SOURCE)
                .userId(user.getId().toString())
                .profileId(profile.getId().toString())
                .userEventType(UserEvent.UserEventType.PROFILE_CREATED)
                .version(1)
                .build();

        sendEvent(event);
    }

    private void sendEvent(UserEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(UserEvent.TOPIC, event.getUserId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send event {} to Kafka: {}", event.getEventType(), ex.getMessage());
            } else {
                log.debug("Event {} sent successfully to partition {} at offset {}",
                        event.getEventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    @SuppressWarnings("unused")
    private void publishEventFallback(User user, Exception ex) {
        log.error("Circuit breaker open for Kafka producer. Event not published for user: {}. Error: {}",
                user.getId(), ex.getMessage());
        // In production, you might want to:
        // 1. Store events in a local queue/database for later retry
        // 2. Send to a DLQ (Dead Letter Queue)
        // 3. Trigger an alert
    }

    @SuppressWarnings("unused")
    private void publishEventFallback(User user, Profile profile, Exception ex) {
        log.error("Circuit breaker open for Kafka producer. Profile event not published for user: {}. Error: {}",
                user.getId(), ex.getMessage());
    }
}
