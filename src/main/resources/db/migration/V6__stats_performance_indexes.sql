-- V6: Performance indexes for statistics and Hibernate optimization
-- Add composite indexes for better query performance

-- Index for deck_daily_stats queries by deck_id and date range
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_deck_date 
ON deck_daily_stats(deck_id, date DESC);

-- Index for deck_daily_stats queries by date (for time-based analytics)
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_date 
ON deck_daily_stats(date DESC);

-- Index for deck_daily_stats queries by performance metrics (for leaderboards)
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_performance 
ON deck_daily_stats(deck_id, correct DESC, viewed DESC);

-- Composite index for user progress tracking
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_user_progress 
ON deck_daily_stats(deck_id, date DESC, sessions DESC);

-- Index for known_cards queries (already exists but optimize)
-- CREATE INDEX IF NOT EXISTS idx_known_cards_deck ON known_cards(deck_id);

-- Index for decks table optimization
CREATE INDEX IF NOT EXISTS idx_decks_created_at 
ON decks(created_at DESC);

-- Index for flashcards table optimization  
CREATE INDEX IF NOT EXISTS idx_flashcards_deck_created 
ON flashcards(deck_id, created_at DESC);

-- Index for user_roles table optimization
CREATE INDEX IF NOT EXISTS idx_user_roles_role 
ON user_roles(role);

-- Statistics for query planner (PostgreSQL specific, ignored by H2)
-- ANALYZE deck_daily_stats;
-- ANALYZE known_cards;
-- ANALYZE decks;
-- ANALYZE flashcards;
-- ANALYZE user_roles;
