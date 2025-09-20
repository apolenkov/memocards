-- V1: Core database schema for Flashcards application
-- This migration creates all core tables with proper relationships and constraints

-- Users table - core user management
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User roles table - role-based access control
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Decks table - user's flashcard collections
CREATE TABLE IF NOT EXISTS decks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Flashcards table - individual cards within decks
CREATE TABLE IF NOT EXISTS flashcards (
    id BIGSERIAL PRIMARY KEY,
    deck_id BIGINT NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    front_text VARCHAR(300) NOT NULL,
    back_text VARCHAR(300) NOT NULL,
    example VARCHAR(500),
    image_url VARCHAR(2048),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Deck daily statistics table - learning progress tracking
CREATE TABLE IF NOT EXISTS deck_daily_stats (
    deck_id BIGINT NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    sessions INT NOT NULL DEFAULT 0,
    viewed INT NOT NULL DEFAULT 0,
    correct INT NOT NULL DEFAULT 0,
    repeat_count INT NOT NULL DEFAULT 0,
    hard INT NOT NULL DEFAULT 0,
    total_duration_ms BIGINT NOT NULL DEFAULT 0,
    total_delay_ms BIGINT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (deck_id, date)
);

-- Known cards table - tracks which cards user has mastered
CREATE TABLE IF NOT EXISTS known_cards (
    id BIGSERIAL PRIMARY KEY,
    deck_id BIGINT NOT NULL REFERENCES decks(id) ON DELETE CASCADE,
    card_id BIGINT NOT NULL REFERENCES flashcards(id) ON DELETE CASCADE
);

-- Prevent duplicates for known cards
ALTER TABLE known_cards
ADD CONSTRAINT uk_known_cards_deck_card UNIQUE (deck_id, card_id);

-- Helpful indexes for performance
CREATE INDEX IF NOT EXISTS idx_known_cards_deck ON known_cards (deck_id);
CREATE INDEX IF NOT EXISTS idx_flashcards_deck_id ON flashcards (deck_id);
CREATE INDEX IF NOT EXISTS idx_decks_user_id ON decks (user_id);

-- News table - application announcements and updates
CREATE TABLE IF NOT EXISTS news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Password reset tokens table - user authentication recovery
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE
);

-- User settings table - per-user preferences
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    preferred_locale_code VARCHAR(20) NOT NULL DEFAULT 'en'
);

-- Create unique constraint for user settings (one row per user)
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_settings_user ON user_settings(user_id);

-- Add check constraints for data integrity
ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_sessions_positive 
CHECK (sessions >= 0);

ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_viewed_positive 
CHECK (viewed >= 0);

ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_correct_positive 
CHECK (correct >= 0);

ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_repeat_positive 
CHECK (repeat_count >= 0);

ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_hard_positive 
CHECK (hard >= 0);

ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_duration_positive 
CHECK (total_duration_ms >= 0);

ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_delay_positive 
CHECK (total_delay_ms >= 0);

-- Business logic constraints
ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_correct_le_viewed 
CHECK (correct <= viewed);

ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_repeat_hard_le_viewed 
CHECK ((repeat_count + hard) <= viewed);

