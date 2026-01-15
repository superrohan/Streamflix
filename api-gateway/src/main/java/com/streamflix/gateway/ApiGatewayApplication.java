package com.streamflix.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Streamflix API Gateway Application.
 *
 * This is the central entry point for all client requests to the Streamflix platform.
 *
 * Responsibilities:
 * - Route requests to appropriate microservices
 * - Authenticate and authorize requests via JWT
 * - Rate limit to protect backend services
 * - Provide circuit breaking for resilience
 * - Support canary deployments via weighted routing
 * - Add correlation IDs for distributed tracing
 * - Handle API versioning
 *
 * Architecture Notes:
 * - Built on Spring Cloud Gateway (reactive/non-blocking)
 * - Uses Redis for distributed rate limiting
 * - Stateless design for horizontal scaling
 * - All routes configured via YAML for GitOps compatibility
 *
 * Scaling Strategy:
 * - Horizontally scalable behind a load balancer
 * - No session state - all state in Redis
 * - Target: 10,000+ requests/second per instance
 *
 * @author Streamflix Engineering
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
