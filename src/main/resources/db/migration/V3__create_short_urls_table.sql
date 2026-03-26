CREATE TABLE short_urls (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    slug            VARCHAR(20)     NOT NULL,
    original_url    TEXT            NOT NULL,
    user_id         UUID            REFERENCES users(id)       ON DELETE SET NULL,
    group_id        UUID            REFERENCES url_groups(id)  ON DELETE SET NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    expires_at      TIMESTAMP,
    password_hash   VARCHAR(255),
    max_clicks      INTEGER,
    click_count     BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_short_urls_slug UNIQUE (slug)
);

CREATE UNIQUE INDEX idx_short_urls_slug       ON short_urls (slug);
CREATE INDEX        idx_short_urls_user_id    ON short_urls (user_id);
CREATE INDEX        idx_short_urls_group_id   ON short_urls (group_id);
CREATE INDEX        idx_short_urls_created_at ON short_urls (created_at);
CREATE INDEX        idx_short_urls_active     ON short_urls (is_active) WHERE is_active = TRUE;
