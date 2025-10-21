-- V2: Performance optimization and indexing strategy
-- This migration adds all necessary indexes for optimal query performance

-- Core foreign key indexes for join operations
CREATE INDEX IF NOT EXISTS idx_decks_user_id ON decks(user_id);
CREATE INDEX IF NOT EXISTS idx_cards_deck_id ON cards(deck_id);
CREATE INDEX IF NOT EXISTS idx_known_cards_deck_id ON known_cards(deck_id);
CREATE INDEX IF NOT EXISTS idx_known_cards_card_id ON known_cards(card_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- User authentication and lookup indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at DESC);

-- Deck management indexes
CREATE INDEX IF NOT EXISTS idx_decks_created_at ON decks(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_decks_updated_at ON decks(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_decks_title ON decks(title);

-- Card content and organization indexes
CREATE INDEX IF NOT EXISTS idx_cards_created_at ON cards(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_cards_updated_at ON cards(updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_cards_deck_created ON cards(deck_id, created_at DESC);

-- Statistics and analytics indexes
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_deck_date ON deck_daily_stats(deck_id, date DESC);
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_date ON deck_daily_stats(date DESC);
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_performance ON deck_daily_stats(deck_id, correct DESC, viewed DESC);
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_user_progress ON deck_daily_stats(deck_id, date DESC, sessions DESC);
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_version ON deck_daily_stats(version);

-- User roles and permissions indexes
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);  -- Critical for JOIN with users table
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role);

-- News and content indexes
CREATE INDEX IF NOT EXISTS idx_news_created_at ON news(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_news_author ON news(author);

-- Password reset token indexes
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- User settings indexes
CREATE INDEX IF NOT EXISTS idx_user_settings_locale ON user_settings(preferred_locale_code);

-- Composite indexes for complex queries
CREATE INDEX IF NOT EXISTS idx_decks_user_created ON decks(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_cards_deck_updated ON cards(deck_id, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_known_cards_deck_card ON known_cards(deck_id, card_id);

ANALYZE users;
ANALYZE decks;
ANALYZE cards;
ANALYZE deck_daily_stats;
ANALYZE known_cards;
ANALYZE user_roles;
ANALYZE news;
ANALYZE password_reset_tokens;
ANALYZE user_settings;

