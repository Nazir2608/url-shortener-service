CREATE TABLE daily_stats (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_url_id    UUID            NOT NULL REFERENCES short_urls(id) ON DELETE CASCADE,
    stat_date       DATE            NOT NULL,
    click_count     INTEGER         NOT NULL DEFAULT 0,
    unique_visitors INTEGER         NOT NULL DEFAULT 0,
    top_country     VARCHAR(2),
    top_device      VARCHAR(20),
    top_referrer    VARCHAR(255),

    CONSTRAINT uq_daily_stats_url_date UNIQUE (short_url_id, stat_date)
);

CREATE INDEX idx_daily_stats_short_url_id ON daily_stats (short_url_id);
CREATE INDEX idx_daily_stats_date         ON daily_stats (stat_date);
