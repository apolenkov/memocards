CREATE TABLE IF NOT EXISTS deck_daily_stats (
    deck_id BIGINT NOT NULL,
    date DATE NOT NULL,
    sessions INT NOT NULL,
    viewed INT NOT NULL,
    correct INT NOT NULL,
    repeat_count INT NOT NULL,
    hard INT NOT NULL,
    total_duration_ms BIGINT NOT NULL,
    total_answer_delay_ms BIGINT NOT NULL,
    PRIMARY KEY (deck_id, date)
);

CREATE TABLE IF NOT EXISTS known_cards (
    id BIGSERIAL PRIMARY KEY,
    deck_id BIGINT NOT NULL,
    card_id BIGINT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_known_cards_deck ON known_cards(deck_id);


