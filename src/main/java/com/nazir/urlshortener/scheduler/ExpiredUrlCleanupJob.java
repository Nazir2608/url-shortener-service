package com.nazir.urlshortener.scheduler;

import com.nazir.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Runs every hour to deactivate expired URLs:
 *   - Time-based expiration (expiresAt < now)
 *   - Click-limit expiration (clickCount >= maxClicks)
 */
@Component
public class ExpiredUrlCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiredUrlCleanupJob.class);

    private final ShortUrlRepository shortUrlRepository;

    public ExpiredUrlCleanupJob(ShortUrlRepository shortUrlRepository) {
        this.shortUrlRepository = shortUrlRepository;
    }

    @Scheduled(cron = "0 0 * * * *")  // every hour at :00
    public void run() {
        log.debug("Running expired URL cleanup");

        try {
            int timeExpired  = shortUrlRepository.deactivateExpiredUrls(Instant.now());
            int clickExpired = shortUrlRepository.deactivateOverLimitUrls();

            if (timeExpired > 0 || clickExpired > 0) {
                log.info("Deactivated {} time-expired and {} click-limited URLs",
                    timeExpired, clickExpired);
            }
        } catch (Exception e) {
            log.error("Expired URL cleanup failed: {}", e.getMessage(), e);
        }
    }
}
