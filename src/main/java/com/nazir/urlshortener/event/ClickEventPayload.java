package com.nazir.urlshortener.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable payload carrying raw data captured at redirect time.
 * Published asynchronously so it does NOT block the redirect response.
 */
public record ClickEventPayload(
    UUID shortUrlId,
    String slug,
    Instant clickedAt,
    String ipAddress,
    String userAgent,
    String referrer,
    String acceptLanguage
) {
    public ClickEventPayload {
        if (shortUrlId == null) throw new IllegalArgumentException("shortUrlId must not be null");
        if (clickedAt == null) clickedAt = Instant.now();
    }
}
