CREATE TABLE url_groups (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100)    NOT NULL,
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_url_groups_user_id ON url_groups (user_id);
