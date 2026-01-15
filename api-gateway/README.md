# Streamflix API Gateway

Central entry point for all client requests to the Streamflix platform.

## Architecture Overview

```
                    ┌─────────────────────────────────────────────────────┐
                    │                   API Gateway                        │
                    │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌───────────┐  │
   Client ─────────▶│  │Correlat.│▶│ Logging │▶│  JWT    │▶│Rate Limit │  │
   Request          │  │   ID    │ │ Filter  │ │  Auth   │ │  Filter   │  │
                    │  └─────────┘ └─────────┘ └─────────┘ └───────────┘  │
                    │                      │                               │
                    │         ┌────────────┴────────────┐                 │
                    │         ▼                         ▼                 │
                    │  ┌─────────────┐          ┌─────────────┐           │
                    │  │   Router    │          │  Circuit    │           │
                    │  │             │◀────────▶│  Breaker    │           │
                    │  └─────────────┘          └─────────────┘           │
                    └─────────────────────────────────────────────────────┘
                                    │
           ┌────────────────────────┼────────────────────────┐
           ▼                        ▼                        ▼
    ┌────────────┐          ┌────────────┐          ┌────────────┐
    │   Auth     │          │  Content   │          │  Playback  │
    │  Service   │          │  Service   │          │  Service   │
    └────────────┘          └────────────┘          └────────────┘
```

## Features

### Authentication & Authorization
- **JWT Validation**: Validates tokens and propagates user context
- **Token Blacklisting**: Redis-backed logout support
- **Role-Based Access**: Route-level RBAC enforcement
- **Profile Requirement**: Enforces profile selection for personalized routes

### Rate Limiting
- **Token Bucket Algorithm**: Configurable per-endpoint limits
- **Redis-Backed**: Distributed rate limiting across gateway instances
- **User-Aware**: Rate limits by user ID (authenticated) or IP (anonymous)

### Resilience
- **Circuit Breakers**: Per-service circuit breakers with Resilience4j
- **Fallback Endpoints**: Graceful degradation responses
- **Retries**: Configurable retry policies for transient failures
- **Timeouts**: Request timeout enforcement

### Routing
- **Dynamic Routes**: YAML-configured route definitions
- **Canary Routing**: Weighted traffic splitting for gradual rollouts
- **API Versioning**: Path-based version routing
- **Load Balancing**: Round-robin distribution (with service discovery)

### Observability
- **Correlation IDs**: End-to-end request tracing
- **Structured Logging**: JSON logs with context
- **Metrics**: Prometheus-compatible metrics export
- **Health Endpoints**: Detailed health checks

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `REDIS_HOST` | Redis server hostname | `localhost` |
| `REDIS_PORT` | Redis server port | `6379` |
| `JWT_SECRET` | JWT signing secret (256-bit minimum) | dev default |
| `AUTH_SERVICE_URI` | Auth service base URL | `http://localhost:8081` |
| `CONTENT_SERVICE_URI` | Content service base URL | `http://localhost:8082` |
| `PLAYBACK_SERVICE_URI` | Playback service base URL | `http://localhost:8083` |
| `RECOMMENDATION_SERVICE_URI` | Recommendation service URL | `http://localhost:8084` |
| `ANALYTICS_SERVICE_URI` | Analytics service URL | `http://localhost:8085` |
| `SEARCH_SERVICE_URI` | Search service URL | `http://localhost:8086` |

### Rate Limit Configuration

```yaml
rate-limit:
  endpoints:
    "/api/v1/auth/login":
      requests-per-second: 5      # Strict for auth
      burst-capacity: 10
    "/api/v1/search/**":
      requests-per-second: 50     # Moderate for search
      burst-capacity: 100
```

### Circuit Breaker Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      contentServiceCircuitBreaker:
        slidingWindowSize: 200        # Requests to evaluate
        failureRateThreshold: 50      # % failures to trip
        waitDurationInOpenState: 20s  # Time before half-open
```

## API Routes

### Public Routes (No Auth)
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/refresh`
- `GET /actuator/health`

### Protected Routes
- `/api/v1/users/**` - User management
- `/api/v1/profiles/**` - Profile management
- `/api/v1/content/**` - Content catalog
- `/api/v1/playback/**` - Playback operations (requires profile)
- `/api/v1/recommendations/**` - Personalized recommendations
- `/api/v1/search/**` - Content search

### Admin Routes
- `/api/v1/analytics/**` - Requires ROLE_ADMIN or ROLE_ANALYTICS_VIEWER

## Scaling Strategy

### Horizontal Scaling
- **Stateless Design**: All state in Redis/external services
- **No Sticky Sessions**: Any instance can handle any request
- **Container-Ready**: Designed for Kubernetes HPA

### Recommended Scaling Triggers
```yaml
# Kubernetes HPA
metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "5000"
```

### Capacity Planning
- **Single Instance**: ~10,000 requests/second
- **Redis Connection Pool**: 50 connections per instance
- **Memory**: 512MB-1GB heap recommended

## Failure Scenarios

### Redis Unavailable
- **Impact**: Rate limiting disabled, token blacklist bypassed
- **Mitigation**: Fail-open design, local cache fallback
- **Recovery**: Automatic reconnection on Redis recovery

### Downstream Service Down
- **Impact**: Circuit breaker opens after threshold
- **Mitigation**: Fallback responses returned
- **Recovery**: Half-open state tests service availability

### High Latency
- **Impact**: Requests timeout after configured duration
- **Mitigation**: Client receives timeout error
- **Monitoring**: Slow call rate tracked in circuit breaker

## Development

### Running Locally
```bash
# Start dependencies
docker-compose up -d redis

# Run gateway
./mvnw spring-boot:run -pl api-gateway
```

### Testing
```bash
# Unit tests
./mvnw test -pl api-gateway

# Integration tests
./mvnw verify -pl api-gateway -P integration-test
```

## Metrics

### Key Metrics
- `gateway_requests_total` - Total requests by route
- `gateway_request_duration_seconds` - Request latency histogram
- `resilience4j_circuitbreaker_state` - Circuit breaker states
- `redis_rate_limiter_requests` - Rate limited requests

### Prometheus Endpoint
```
GET /actuator/prometheus
```

## Trade-offs

### JWT vs Session Tokens
**Decision**: JWT tokens for stateless auth
- **Pro**: No session store needed, scales easily
- **Con**: Cannot instantly invalidate (mitigated by blacklist)

### Redis Rate Limiting vs In-Memory
**Decision**: Redis-backed rate limiting
- **Pro**: Works across all gateway instances
- **Con**: Redis dependency, network latency
- **Mitigation**: Fail-open when Redis unavailable

### Circuit Breaker Per-Service vs Global
**Decision**: Per-service circuit breakers
- **Pro**: Isolated failures, fine-grained control
- **Con**: More configuration, more circuits to monitor
