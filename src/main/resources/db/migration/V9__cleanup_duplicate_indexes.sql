-- Cleanup duplicate indexes on known_cards table
-- This migration removes redundant indexes that duplicate functionality
-- and slow down INSERT/UPDATE operations without providing any benefit.

-- Drop duplicate index on deck_id (keep idx_known_cards_deck)
DROP INDEX IF EXISTS idx_known_cards_deck_id;

-- Drop duplicate composite index (keep idx_known_cards_composite)
-- Note: uk_known_cards_deck_card UNIQUE constraint already provides index functionality
DROP INDEX IF EXISTS idx_known_cards_deck_card;

-- Explanation:
-- 1. idx_known_cards_deck_id is duplicate of idx_known_cards_deck (both index deck_id)
-- 2. idx_known_cards_deck_card is duplicate of idx_known_cards_composite (both index deck_id, card_id)
-- 3. uk_known_cards_deck_card UNIQUE constraint already provides B-tree index on (deck_id, card_id)
--    so idx_known_cards_composite is also technically redundant, but we keep it for explicit naming

-- Performance impact:
-- - Faster INSERTs (fewer indexes to update)
-- - Faster UPDATEs (fewer indexes to update)
-- - Less storage usage
-- - No negative impact on query performance (kept optimal indexes)

