CREATE TABLE rate_limit_config (
    tier                      VARCHAR(20)  PRIMARY KEY,
    requests_per_minute       INTEGER      NOT NULL,
    urls_per_day              INTEGER      NOT NULL,
    max_custom_slugs          INTEGER      NOT NULL,
    analytics_retention_days  INTEGER      NOT NULL
);

INSERT INTO rate_limit_config (tier, requests_per_minute, urls_per_day, max_custom_slugs, analytics_retention_days) VALUES
    ('ANONYMOUS',   10,    5,    0,     7),
    ('FREE',        60,    50,   10,    30),
    ('PRO',         300,   500,  100,   365),
    ('ENTERPRISE',  1000,  5000, 1000,  730);
