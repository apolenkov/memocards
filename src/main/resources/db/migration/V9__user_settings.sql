-- Create table for per-user settings (preferred locale, etc.)
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    preferred_locale_code VARCHAR(20) NOT NULL
);

-- Unique index to ensure single settings row per user
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_settings_user ON user_settings(user_id);


