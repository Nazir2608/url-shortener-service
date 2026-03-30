package com.nazir.urlshortener.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes click events to Spring's internal ApplicationEvent bus.
 * The listener processes them asynchronously on a virtual thread.
 *
 * This is a clean single-node alternative to Kafka.
 * To scale horizontally, swap this for KafkaTemplate.send().
 */
@Component
public class ClickEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClickEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public ClickEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish(ClickEventPayload payload) {
        log.debug("Publishing click event for slug='{}' from IP='{}'",
            payload.slug(), payload.ipAddress());
        eventPublisher.publishEvent(payload);
    }
}
