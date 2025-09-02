-- Remove repeat_count column and related constraints from deck_daily_stats table
-- This simplifies the statistics logic by removing the confusing "repetitions" concept

-- Drop the constraint that includes repeat_count
ALTER TABLE deck_daily_stats 
DROP CONSTRAINT IF EXISTS chk_deck_daily_stats_repeat_hard_le_viewed;

-- Drop the repeat_count column
ALTER TABLE deck_daily_stats 
DROP COLUMN IF EXISTS repeat_count;

-- Add a simpler constraint that only checks hard <= viewed
ALTER TABLE deck_daily_stats 
ADD CONSTRAINT chk_deck_daily_stats_hard_le_viewed 
CHECK (hard <= viewed);
