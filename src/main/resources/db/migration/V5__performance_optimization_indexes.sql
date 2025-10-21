-- V5: Performance optimization indexes for cards pagination and filtering
-- This migration adds composite indexes to optimize:
-- 1. Card listing with pagination (deck_id + created_at for ORDER BY)
-- 2. Known cards JOIN queries (deck_id + card_id)

-- Composite index for cards pagination and sorting
-- Supports: SELECT * FROM cards WHERE deck_id = ? ORDER BY created_at LIMIT ? OFFSET ?
CREATE INDEX IF NOT EXISTS idx_cards_deck_created 
ON cards (deck_id, created_at);

-- Composite index for known_cards JOIN optimization
-- Supports: JOIN known_cards kc ON kc.card_id = f.id AND kc.deck_id = ?
-- Also optimizes: SELECT card_id FROM known_cards WHERE deck_id = ?
CREATE INDEX IF NOT EXISTS idx_known_cards_composite 
ON known_cards (deck_id, card_id);

-- Comment explaining the optimization
COMMENT ON INDEX idx_cards_deck_created IS 
'Composite index for efficient card pagination and sorting by creation date within deck';

COMMENT ON INDEX idx_known_cards_composite IS 
'Composite index for optimizing known_cards JOIN queries and deck-specific lookups';

