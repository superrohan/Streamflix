package com.streamflix.common.security;

/**
 * Thread-local holder for user context.
 *
 * This enables access to user information anywhere in the request
 * processing chain without passing it explicitly through method parameters.
 *
 * IMPORTANT: The context must be cleared after request processing
 * to prevent memory leaks in thread pools.
 */
public final class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {
        // Prevent instantiation
    }

    /**
     * Set the user context for the current thread.
     */
    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    /**
     * Get the user context for the current thread.
     *
     * @return UserContext or null if not set
     */
    public static UserContext getContext() {
        return CONTEXT.get();
    }

    /**
     * Get the user context, throwing if not present.
     *
     * @throws IllegalStateException if no context is set
     */
    public static UserContext requireContext() {
        UserContext context = CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("No UserContext available - request may not be authenticated");
        }
        return context;
    }

    /**
     * Clear the context for the current thread.
     * MUST be called after request processing.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Get the current user ID, or null if not authenticated.
     */
    public static String getUserId() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getUserId() : null;
    }

    /**
     * Get the current profile ID, or null if not selected.
     */
    public static String getProfileId() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getProfileId() : null;
    }

    /**
     * Get the correlation ID for the current request.
     */
    public static String getCorrelationId() {
        UserContext context = CONTEXT.get();
        return context != null ? context.getCorrelationId() : null;
    }
}
