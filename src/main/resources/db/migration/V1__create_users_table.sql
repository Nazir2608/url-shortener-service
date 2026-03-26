CREATE TABLE users (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    api_key         VARCHAR(255),
    tier            VARCHAR(20)     NOT NULL DEFAULT 'FREE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email   UNIQUE (email),
    CONSTRAINT uq_users_api_key UNIQUE (api_key)
);

CREATE INDEX idx_users_email   ON users (email);
CREATE INDEX idx_users_api_key ON users (api_key);
CREATE INDEX idx_users_tier    ON users (tier);
