-- Streamflix Analytics Service Database Schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Hourly aggregated metrics
CREATE TABLE hourly_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_hour TIMESTAMP WITH TIME ZONE NOT NULL,
    total_views BIGINT DEFAULT 0,
    unique_viewers BIGINT DEFAULT 0,
    total_watch_minutes BIGINT DEFAULT 0,
    avg_session_minutes DECIMAL(10,2) DEFAULT 0,
    new_users BIGINT DEFAULT 0,
    active_users BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (metric_hour)
);

CREATE INDEX idx_hourly_metrics_hour ON hourly_metrics(metric_hour);

-- Daily aggregated metrics
CREATE TABLE daily_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_date DATE NOT NULL,
    total_views BIGINT DEFAULT 0,
    unique_viewers BIGINT DEFAULT 0,
    total_watch_minutes BIGINT DEFAULT 0,
    avg_session_minutes DECIMAL(10,2) DEFAULT 0,
    peak_concurrent_viewers BIGINT DEFAULT 0,
    new_users BIGINT DEFAULT 0,
    churned_users BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (metric_date)
);

CREATE INDEX idx_daily_metrics_date ON daily_metrics(metric_date);

-- Content performance metrics
CREATE TABLE content_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID NOT NULL,
    metric_date DATE NOT NULL,
    view_count BIGINT DEFAULT 0,
    unique_viewers BIGINT DEFAULT 0,
    total_watch_minutes BIGINT DEFAULT 0,
    avg_watch_percentage DECIMAL(5,2) DEFAULT 0,
    completion_count BIGINT DEFAULT 0,
    completion_rate DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (content_id, metric_date)
);

CREATE INDEX idx_content_metrics_content ON content_metrics(content_id);
CREATE INDEX idx_content_metrics_date ON content_metrics(metric_date);

-- Genre performance metrics
CREATE TABLE genre_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    genre_id UUID NOT NULL,
    metric_date DATE NOT NULL,
    view_count BIGINT DEFAULT 0,
    unique_viewers BIGINT DEFAULT 0,
    total_watch_minutes BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (genre_id, metric_date)
);

-- Real-time metrics (current hour)
CREATE TABLE realtime_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(20,4) NOT NULL,
    metric_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    tags JSONB DEFAULT '{}'
);

CREATE INDEX idx_realtime_metrics_name ON realtime_metrics(metric_name);
CREATE INDEX idx_realtime_metrics_timestamp ON realtime_metrics(metric_timestamp);
