-- Add indexes for known_cards table to optimize JOIN queries
-- These indexes significantly improve performance for KNOWN/UNKNOWN filtering

-- Index for filtering unknown cards (LEFT JOIN ... WHERE kc.id IS NULL)
CREATE INDEX IF NOT EXISTS idx_known_cards_card_deck ON known_cards (card_id, deck_id);

-- Index for filtering known cards (INNER JOIN)
CREATE INDEX IF NOT EXISTS idx_known_cards_deck_card ON known_cards (deck_id, card_id);

-- ANALYZE table to update statistics for query planner
ANALYZE known_cards;

