-- Streamflix Content Catalog Database Schema
-- Version: 1.0
-- Description: Initial schema for movies, series, episodes, and genres

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Genres
CREATE TABLE genres (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Content (base table for movies and series)
CREATE TABLE content (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_type VARCHAR(20) NOT NULL,  -- MOVIE, SERIES
    title VARCHAR(500) NOT NULL,
    original_title VARCHAR(500),
    slug VARCHAR(500) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    release_year INTEGER,
    release_date DATE,
    runtime_minutes INTEGER,  -- For movies, average for series
    maturity_rating VARCHAR(20) NOT NULL DEFAULT 'ALL',
    poster_url VARCHAR(500),
    backdrop_url VARCHAR(500),
    trailer_url VARCHAR(500),
    imdb_id VARCHAR(20),
    tmdb_id INTEGER,
    average_rating DECIMAL(3,2),
    rating_count INTEGER DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    popularity_score DECIMAL(10,4) DEFAULT 0,
    is_original BOOLEAN DEFAULT FALSE,  -- Streamflix Original
    is_featured BOOLEAN DEFAULT FALSE,
    is_published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP WITH TIME ZONE,
    available_from TIMESTAMP WITH TIME ZONE,
    available_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_content_type ON content(content_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_content_slug ON content(slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_content_published ON content(is_published, published_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_content_popularity ON content(popularity_score DESC) WHERE deleted_at IS NULL AND is_published = TRUE;
CREATE INDEX idx_content_release_year ON content(release_year DESC) WHERE deleted_at IS NULL AND is_published = TRUE;
CREATE INDEX idx_content_maturity ON content(maturity_rating) WHERE deleted_at IS NULL AND is_published = TRUE;

-- Content-Genre relationship
CREATE TABLE content_genres (
    content_id UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    genre_id UUID NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    is_primary BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (content_id, genre_id)
);

CREATE INDEX idx_content_genres_genre ON content_genres(genre_id);

-- Seasons (for series)
CREATE TABLE seasons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    series_id UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    season_number INTEGER NOT NULL,
    title VARCHAR(500),
    description TEXT,
    poster_url VARCHAR(500),
    episode_count INTEGER DEFAULT 0,
    release_date DATE,
    is_published BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (series_id, season_number)
);

CREATE INDEX idx_seasons_series ON seasons(series_id);

-- Episodes
CREATE TABLE episodes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    season_id UUID NOT NULL REFERENCES seasons(id) ON DELETE CASCADE,
    episode_number INTEGER NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    runtime_minutes INTEGER NOT NULL,
    thumbnail_url VARCHAR(500),
    video_url VARCHAR(500),  -- HLS/DASH manifest URL
    release_date DATE,
    is_published BOOLEAN DEFAULT FALSE,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (season_id, episode_number)
);

CREATE INDEX idx_episodes_season ON episodes(season_id);

-- Cast and Crew
CREATE TABLE people (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    biography TEXT,
    birth_date DATE,
    death_date DATE,
    birth_place VARCHAR(255),
    photo_url VARCHAR(500),
    imdb_id VARCHAR(20),
    tmdb_id INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_people_name ON people(name);

-- Content Credits (cast and crew)
CREATE TABLE content_credits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    person_id UUID NOT NULL REFERENCES people(id) ON DELETE CASCADE,
    credit_type VARCHAR(50) NOT NULL,  -- ACTOR, DIRECTOR, WRITER, PRODUCER, etc.
    role_name VARCHAR(255),  -- Character name for actors
    display_order INTEGER DEFAULT 0,
    is_lead BOOLEAN DEFAULT FALSE,
    UNIQUE (content_id, person_id, credit_type, role_name)
);

CREATE INDEX idx_content_credits_content ON content_credits(content_id);
CREATE INDEX idx_content_credits_person ON content_credits(person_id);

-- Video Streams (multiple qualities)
CREATE TABLE video_streams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    quality VARCHAR(20) NOT NULL,  -- SD, HD, FHD, UHD
    codec VARCHAR(50) NOT NULL,  -- H264, H265, VP9, AV1
    bitrate_kbps INTEGER NOT NULL,
    manifest_url VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CHECK (content_id IS NOT NULL OR episode_id IS NOT NULL)
);

-- Audio Tracks
CREATE TABLE audio_tracks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL,
    language_name VARCHAR(100) NOT NULL,
    audio_type VARCHAR(50) DEFAULT 'ORIGINAL',  -- ORIGINAL, DUBBED
    codec VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CHECK (content_id IS NOT NULL OR episode_id IS NOT NULL)
);

-- Subtitles
CREATE TABLE subtitles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    content_id UUID REFERENCES content(id) ON DELETE CASCADE,
    episode_id UUID REFERENCES episodes(id) ON DELETE CASCADE,
    language_code VARCHAR(10) NOT NULL,
    language_name VARCHAR(100) NOT NULL,
    subtitle_type VARCHAR(50) DEFAULT 'FULL',  -- FULL, FORCED, SDH
    format VARCHAR(20) NOT NULL,  -- VTT, SRT
    url VARCHAR(500) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CHECK (content_id IS NOT NULL OR episode_id IS NOT NULL)
);

-- Content Tags (for search and filtering)
CREATE TABLE content_tags (
    content_id UUID NOT NULL REFERENCES content(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (content_id, tag)
);

CREATE INDEX idx_content_tags_tag ON content_tags(tag);

-- Insert default genres
INSERT INTO genres (name, slug, description, display_order) VALUES
    ('Action', 'action', 'High-energy action content', 1),
    ('Comedy', 'comedy', 'Laugh-out-loud funny content', 2),
    ('Drama', 'drama', 'Compelling dramatic stories', 3),
    ('Thriller', 'thriller', 'Suspenseful and thrilling content', 4),
    ('Horror', 'horror', 'Scary and horror content', 5),
    ('Sci-Fi', 'sci-fi', 'Science fiction and futuristic', 6),
    ('Fantasy', 'fantasy', 'Magical and fantastical worlds', 7),
    ('Romance', 'romance', 'Love stories and romantic content', 8),
    ('Documentary', 'documentary', 'Real-world documentaries', 9),
    ('Animation', 'animation', 'Animated content for all ages', 10),
    ('Crime', 'crime', 'Crime and mystery stories', 11),
    ('Family', 'family', 'Family-friendly content', 12),
    ('Kids', 'kids', 'Content for children', 13);

-- Function to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers
CREATE TRIGGER update_content_updated_at BEFORE UPDATE ON content
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_genres_updated_at BEFORE UPDATE ON genres
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_seasons_updated_at BEFORE UPDATE ON seasons
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_episodes_updated_at BEFORE UPDATE ON episodes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
