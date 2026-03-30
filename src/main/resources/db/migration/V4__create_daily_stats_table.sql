-- ═══════════════════════════════════════════════════════════════
-- V4: Daily Stats — pre-aggregated daily summary per URL
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE daily_stats
(
    id              BIGSERIAL    PRIMARY KEY,
    short_url_id    UUID         NOT NULL REFERENCES short_urls (id) ON DELETE CASCADE,
    date            DATE         NOT NULL,
    click_count     INTEGER      NOT NULL DEFAULT 0,
    unique_visitors INTEGER      NOT NULL DEFAULT 0,
    top_country     VARCHAR(2),
    top_device      VARCHAR(20),
    top_referrer    VARCHAR(255),

    CONSTRAINT uq_daily_stats_url_date UNIQUE (short_url_id, date)
);

CREATE INDEX idx_daily_stats_short_url_id ON daily_stats (short_url_id);
CREATE INDEX idx_daily_stats_date         ON daily_stats (date);

COMMENT ON TABLE daily_stats IS 'Pre-aggregated daily analytics populated by scheduled job';
