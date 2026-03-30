-- ═══════════════════════════════════════════════════════════════
-- V3: Click Events — every redirect click with full metadata
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE click_events
(
    id              BIGSERIAL    PRIMARY KEY,
    short_url_id    UUID         NOT NULL REFERENCES short_urls (id) ON DELETE CASCADE,
    clicked_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    -- Network
    ip_address      VARCHAR(45),
    language        VARCHAR(10),

    -- Geo (populated async via MaxMind)
    country         VARCHAR(2),
    city            VARCHAR(100),
    region          VARCHAR(100),
    latitude        DECIMAL(9, 6),
    longitude       DECIMAL(9, 6),

    -- Device (populated async via Yauaa)
    device_type     VARCHAR(20),
    os_name         VARCHAR(50),
    os_version      VARCHAR(20),
    browser_name    VARCHAR(50),
    browser_version VARCHAR(20),

    -- Referrer
    referrer        TEXT,
    referrer_domain VARCHAR(255),

    -- Raw header for reprocessing
    user_agent      TEXT
);

-- ═══ Indexes ═══
CREATE INDEX idx_click_events_short_url_id ON click_events (short_url_id);
CREATE INDEX idx_click_events_clicked_at   ON click_events (clicked_at);
CREATE INDEX idx_click_events_country      ON click_events (country);
CREATE INDEX idx_click_events_device_type  ON click_events (device_type);

-- Composite index for time-range analytics per URL
CREATE INDEX idx_click_events_url_time ON click_events (short_url_id, clicked_at);

COMMENT ON TABLE click_events IS 'Stores every redirect click with geo, device, and referrer metadata';
