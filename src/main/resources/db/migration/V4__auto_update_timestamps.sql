-- V4: Auto-update timestamps trigger
-- This migration adds PostgreSQL trigger to automatically update updated_at column

-- Create function that updates updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply trigger to decks table
CREATE TRIGGER update_decks_updated_at
    BEFORE UPDATE ON decks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to flashcards table
CREATE TRIGGER update_flashcards_updated_at
    BEFORE UPDATE ON flashcards
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to news table
CREATE TRIGGER update_news_updated_at
    BEFORE UPDATE ON news
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Apply trigger to deck_daily_stats table
CREATE TRIGGER update_deck_daily_stats_updated_at
    BEFORE UPDATE ON deck_daily_stats
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

