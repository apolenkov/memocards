-- Create password reset tokens table for PostgreSQL
CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on token for fast lookups
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Create index on user_id for cleanup operations
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- Create index on expires_at for cleanup operations
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
