package com.nazir.urlshortener.event;

import com.nazir.urlshortener.service.ClickTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async listener for click events.
 *
 * @Async causes this to run on a Virtual Thread (Java 21),
 * completely decoupled from the HTTP redirect response.
 *
 * Pipeline:
 *   1. GeoIP lookup (IP → country, city, coordinates)
 *   2. User-Agent parsing (UA → device type, OS, browser)
 *   3. Referrer domain extraction
 *   4. Persist ClickEvent to PostgreSQL
 *   5. Increment denormalized click counter on ShortUrl
 */
@Component
public class ClickEventListener {

    private static final Logger log = LoggerFactory.getLogger(ClickEventListener.class);

    private final ClickTrackingService clickTrackingService;

    public ClickEventListener(ClickTrackingService clickTrackingService) {
        this.clickTrackingService = clickTrackingService;
    }

    @Async
    @EventListener
    public void handleClickEvent(ClickEventPayload payload) {
        try {
            log.debug("Processing click event for slug='{}', IP='{}'",
                payload.slug(), payload.ipAddress());
            clickTrackingService.track(payload);
        } catch (Exception e) {
            // Never let tracking failures propagate — log and move on
            log.error("Failed to process click event for slug='{}': {}",
                payload.slug(), e.getMessage(), e);
        }
    }
}
