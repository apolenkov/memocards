-- V5: Add missing index on user_roles(user_id)
-- Critical for JOIN performance in SELECT_USER_WITH_ROLES_BY_EMAIL query

-- Add index on user_id for efficient JOIN with users table
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

-- Composite index for covering index optimization (optional but recommended)
CREATE INDEX IF NOT EXISTS idx_user_roles_user_role ON user_roles(user_id, role);

