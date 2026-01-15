-- Streamflix Playback Service Database Schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Watch Progress (for resume playback)
CREATE TABLE watch_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID NOT NULL,
    content_id UUID NOT NULL,
    episode_id UUID,  -- NULL for movies
    position_seconds BIGINT NOT NULL DEFAULT 0,
    duration_seconds BIGINT NOT NULL,
    watch_percentage INTEGER NOT NULL DEFAULT 0,
    is_completed BOOLEAN DEFAULT FALSE,
    last_watched_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, content_id, episode_id)
);

CREATE INDEX idx_watch_progress_profile ON watch_progress(profile_id);
CREATE INDEX idx_watch_progress_content ON watch_progress(content_id);
CREATE INDEX idx_watch_progress_last_watched ON watch_progress(profile_id, last_watched_at DESC);

-- Watch History (completed views)
CREATE TABLE watch_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID NOT NULL,
    content_id UUID NOT NULL,
    episode_id UUID,
    watched_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    watch_duration_seconds BIGINT NOT NULL,
    device_type VARCHAR(50),
    device_id VARCHAR(255)
);

CREATE INDEX idx_watch_history_profile ON watch_history(profile_id);
CREATE INDEX idx_watch_history_profile_date ON watch_history(profile_id, watched_at DESC);

-- Active Streams (for concurrent limit enforcement)
CREATE TABLE active_streams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    content_id UUID NOT NULL,
    episode_id UUID,
    device_id VARCHAR(255) NOT NULL,
    device_type VARCHAR(50),
    device_name VARCHAR(100),
    stream_started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_heartbeat_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    quality_level VARCHAR(20),
    ip_address VARCHAR(45)
);

CREATE INDEX idx_active_streams_user ON active_streams(user_id);
CREATE UNIQUE INDEX idx_active_streams_device ON active_streams(user_id, device_id);

-- Function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_watch_progress_updated_at
    BEFORE UPDATE ON watch_progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
