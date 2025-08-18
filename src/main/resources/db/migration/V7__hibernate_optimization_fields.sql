-- V7: Add Hibernate optimization fields for better performance and audit
-- Add version field for optimistic locking
-- Add audit fields for tracking creation and updates

-- Add version column for optimistic locking
ALTER TABLE deck_daily_stats 
ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Add created_at and updated_at columns for audit
ALTER TABLE deck_daily_stats 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE deck_daily_stats 
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Update existing records to have proper timestamps
UPDATE deck_daily_stats 
SET created_at = CURRENT_TIMESTAMP, 
    updated_at = CURRENT_TIMESTAMP 
WHERE created_at IS NULL;

-- Make audit fields NOT NULL after populating
ALTER TABLE deck_daily_stats 
ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE deck_daily_stats 
ALTER COLUMN updated_at SET NOT NULL;

-- Add trigger for automatic updated_at updates (PostgreSQL specific, ignored by H2)
-- CREATE OR REPLACE FUNCTION update_updated_at_column()
-- RETURNS TRIGGER AS $$
-- BEGIN
--     NEW.updated_at = CURRENT_TIMESTAMP;
--     RETURN NEW;
-- END;
-- $$ language 'plpgsql';
-- 
-- CREATE TRIGGER update_deck_daily_stats_updated_at 
--     BEFORE UPDATE ON deck_daily_stats 
--     FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

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
CHECK (total_answer_delay_ms >= 0);

-- Add constraint that correct cannot exceed viewed
ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_correct_le_viewed 
CHECK (correct <= viewed);

-- Add constraint that repeat + hard cannot exceed viewed
ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_repeat_hard_le_viewed 
CHECK ((repeat_count + hard) <= viewed);

-- Add index on version for optimistic locking performance
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_version 
ON deck_daily_stats(version);

-- Add index on audit fields for time-based queries
CREATE INDEX IF NOT EXISTS idx_deck_daily_stats_audit 
ON deck_daily_stats(created_at DESC, updated_at DESC);
