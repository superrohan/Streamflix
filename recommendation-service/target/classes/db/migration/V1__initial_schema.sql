-- Streamflix Recommendation Service Database Schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Profile viewing preferences (aggregated from events)
CREATE TABLE profile_preferences (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID NOT NULL UNIQUE,
    genre_preferences JSONB DEFAULT '{}',  -- {"action": 0.8, "comedy": 0.5}
    content_type_preferences JSONB DEFAULT '{}',  -- {"MOVIE": 0.6, "SERIES": 0.4}
    average_watch_time_minutes INTEGER DEFAULT 0,
    total_content_watched INTEGER DEFAULT 0,
    last_activity_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_profile_preferences_profile ON profile_preferences(profile_id);

-- Content popularity scores (time-windowed)
CREATE TABLE content_popularity (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID NOT NULL,
    time_window VARCHAR(20) NOT NULL,  -- HOUR, DAY, WEEK
    view_count BIGINT DEFAULT 0,
    unique_viewers BIGINT DEFAULT 0,
    completion_rate DECIMAL(5,2) DEFAULT 0,
    popularity_score DECIMAL(10,4) DEFAULT 0,
    window_start TIMESTAMP WITH TIME ZONE NOT NULL,
    window_end TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (content_id, time_window, window_start)
);

CREATE INDEX idx_content_popularity_content ON content_popularity(content_id);
CREATE INDEX idx_content_popularity_window ON content_popularity(time_window, window_start);
CREATE INDEX idx_content_popularity_score ON content_popularity(popularity_score DESC);

-- Trending content cache
CREATE TABLE trending_content (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID NOT NULL,
    rank INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,  -- OVERALL, MOVIES, SERIES, genre slugs
    score DECIMAL(10,4) NOT NULL,
    view_count_24h BIGINT DEFAULT 0,
    calculated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (category, rank)
);

CREATE INDEX idx_trending_content_category ON trending_content(category, rank);

-- Similar content relationships
CREATE TABLE content_similarity (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID NOT NULL,
    similar_content_id UUID NOT NULL,
    similarity_score DECIMAL(5,4) NOT NULL,  -- 0.0 to 1.0
    similarity_reason VARCHAR(100),  -- "shared_genres", "same_director", "viewing_pattern"
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (content_id, similar_content_id)
);

CREATE INDEX idx_content_similarity_content ON content_similarity(content_id);
CREATE INDEX idx_content_similarity_score ON content_similarity(content_id, similarity_score DESC);

-- Personalized recommendations cache
CREATE TABLE personalized_recommendations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    profile_id UUID NOT NULL,
    content_id UUID NOT NULL,
    score DECIMAL(10,4) NOT NULL,
    reason VARCHAR(255),  -- "Because you watched X", "Popular in Action"
    reason_content_id UUID,
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (profile_id, content_id)
);

CREATE INDEX idx_personalized_recommendations_profile ON personalized_recommendations(profile_id, score DESC);

-- Viewing events log (for batch processing)
CREATE TABLE viewing_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id VARCHAR(255) NOT NULL UNIQUE,
    profile_id UUID NOT NULL,
    content_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    watch_percentage INTEGER,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_viewing_events_profile ON viewing_events(profile_id);
CREATE INDEX idx_viewing_events_content ON viewing_events(content_id);
CREATE INDEX idx_viewing_events_timestamp ON viewing_events(event_timestamp);
CREATE INDEX idx_viewing_events_unprocessed ON viewing_events(processed) WHERE processed = FALSE;

-- Function to update timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_profile_preferences_updated_at
    BEFORE UPDATE ON profile_preferences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
