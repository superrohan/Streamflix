package com.streamflix.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * Event emitted for user and profile lifecycle operations.
 *
 * Used for:
 * - User registration notifications
 * - Profile creation/updates
 * - Session tracking
 * - Security auditing
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEvent extends DomainEvent {

    public static final String TOPIC = "streamflix.user.events";

    private String userId;
    private String profileId;
    private String email;
    private String username;
    private UserEventType userEventType;
    private List<String> roles;
    private Map<String, Object> metadata;

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public String getPartitionKey() {
        return userId;
    }

    public enum UserEventType {
        USER_REGISTERED,
        USER_LOGIN,
        USER_LOGOUT,
        PROFILE_CREATED,
        PROFILE_UPDATED,
        PROFILE_DELETED,
        PASSWORD_CHANGED,
        SUBSCRIPTION_CHANGED
    }
}
