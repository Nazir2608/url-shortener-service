package com.nazir.urlshortener.service;

import com.nazir.urlshortener.domain.ShortUrl;
import com.nazir.urlshortener.dto.mapper.ShortUrlMapper;
import com.nazir.urlshortener.dto.response.LinkPreviewResponse;
import com.nazir.urlshortener.exception.ShortUrlExpiredException;
import com.nazir.urlshortener.exception.ShortUrlNotFoundException;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles URL resolution and redirection logic.
 * <p>
 * Separated from {@link ShortUrlService} because:
 * <ul>
 *   <li>This is the HOT PATH — performance-critical (target: &lt;50ms)</li>
 *   <li>Phase 3 adds Redis caching here</li>
 *   <li>Phase 4 adds async click tracking here</li>
 *   <li>Different transactional requirements</li>
 * </ul>
 */
@Service
public class RedirectService {

    private static final Logger log = LoggerFactory.getLogger(RedirectService.class);

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlMapper shortUrlMapper;

    public RedirectService(ShortUrlRepository shortUrlRepository,
                           ShortUrlMapper shortUrlMapper) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortUrlMapper = shortUrlMapper;
    }

    /**
     * Resolve a slug to its original URL and increment the click counter.
     * <p>
     * Phase 2: Synchronous DB lookup + synchronous counter increment.
     * Phase 3: Redis cache lookup first, DB fallback.
     * Phase 4: Async click event publishing (GeoIP, UA parsing).
     * </p>
     *
     * @param slug the short URL slug
     * @return the original URL to redirect to
     * @throws ShortUrlNotFoundException if slug doesn't exist
     * @throws ShortUrlExpiredException  if link is expired, inactive, or max-clicked
     */
    @Transactional
    public String resolveAndTrack(String slug) {
        // 1. Look up the slug
        ShortUrl shortUrl = shortUrlRepository.findBySlug(slug)
            .orElseThrow(() -> new ShortUrlNotFoundException(slug));

        // 2. Check if accessible
        if (!shortUrl.isAccessible()) {
            if (!shortUrl.isActive()) {
                throw new ShortUrlExpiredException(slug);
            }
            if (shortUrl.isExpired()) {
                // Deactivate expired links on access
                shortUrl.setActive(false);
                shortUrlRepository.save(shortUrl);
                throw new ShortUrlExpiredException(slug);
            }
            if (shortUrl.hasReachedMaxClicks()) {
                shortUrl.setActive(false);
                shortUrlRepository.save(shortUrl);
                throw new ShortUrlExpiredException(slug);
            }
        }

        // 3. Password-protected links — Phase 5 will handle auth flow
        //    For now, password-protected links redirect normally
        //    (the password check will be added in the controller in Phase 5)

        // 4. Increment click count (atomic DB update, no race condition)
        shortUrlRepository.incrementClickCount(shortUrl.getId());

        // 5. TODO Phase 4: Publish async click event for analytics
        //    clickEventPublisher.publish(new ClickEventPayload(slug, ip, userAgent, referer));

        log.debug("Resolved slug '{}' → '{}'", slug, shortUrl.getOriginalUrl());
        return shortUrl.getOriginalUrl();
    }

    /**
     * Get link preview without redirecting or counting a click.
     *
     * @param slug the short URL slug
     * @return preview information about the link
     * @throws ShortUrlNotFoundException if slug doesn't exist
     */
    @Transactional(readOnly = true)
    public LinkPreviewResponse getPreview(String slug) {
        ShortUrl shortUrl = shortUrlRepository.findBySlug(slug)
            .orElseThrow(() -> new ShortUrlNotFoundException(slug));

        return shortUrlMapper.toPreview(shortUrl);
    }
}
