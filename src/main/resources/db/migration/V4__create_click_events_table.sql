CREATE TABLE click_events (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    short_url_id    UUID            NOT NULL REFERENCES short_urls(id) ON DELETE CASCADE,
    clicked_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address      VARCHAR(45),
    country         VARCHAR(2),
    city            VARCHAR(100),
    region          VARCHAR(100),
    latitude        DECIMAL(9,6),
    longitude       DECIMAL(9,6),
    device_type     VARCHAR(20),
    os_name         VARCHAR(50),
    os_version      VARCHAR(20),
    browser_name    VARCHAR(50),
    browser_version VARCHAR(20),
    referrer        TEXT,
    referrer_domain VARCHAR(255),
    user_agent      TEXT,
    language        VARCHAR(10)
);

CREATE INDEX idx_click_events_short_url_id  ON click_events (short_url_id);
CREATE INDEX idx_click_events_clicked_at    ON click_events (clicked_at);
CREATE INDEX idx_click_events_country       ON click_events (country);
CREATE INDEX idx_click_events_device_type   ON click_events (device_type);
CREATE INDEX idx_click_events_referrer_dom  ON click_events (referrer_domain);
