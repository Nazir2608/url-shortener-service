package com.nazir.urlshortener.service;

import com.nazir.urlshortener.domain.ShortUrl;
import com.nazir.urlshortener.dto.cache.CachedShortUrl;
import com.nazir.urlshortener.dto.mapper.ShortUrlMapper;
import com.nazir.urlshortener.dto.response.LinkPreviewResponse;
import com.nazir.urlshortener.exception.ShortUrlExpiredException;
import com.nazir.urlshortener.exception.ShortUrlNotFoundException;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Handles URL resolution and redirection — the HOT PATH.
 *
 * <h3>Phase 3 Cache-Aside Pattern:</h3>
 * <pre>
 * 1. Check Redis cache for slug
 * 2. CACHE HIT  → validate accessibility from cached data → redirect
 * 3. CACHE MISS → query database → populate cache → redirect
 * 4. Always increment click count in DB (authoritative counter)
 * </pre>
 *
 * <h3>Caching Rules:</h3>
 * <ul>
 *   <li>URLs with {@code maxClicks} are NEVER cached (need real-time count)</li>
 *   <li>Expired/inactive URLs are evicted from cache on access</li>
 *   <li>Cache TTL = min(configured TTL, time until URL expiry)</li>
 *   <li>If Redis is down, all requests fall through to DB (graceful degradation)</li>
 * </ul>
 */
@Service
public class RedirectService {

    private static final Logger log = LoggerFactory.getLogger(RedirectService.class);

    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlMapper shortUrlMapper;
    private final UrlCacheService urlCacheService;

    public RedirectService(ShortUrlRepository shortUrlRepository, ShortUrlMapper shortUrlMapper, UrlCacheService urlCacheService) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortUrlMapper = shortUrlMapper;
        this.urlCacheService = urlCacheService;
    }

    /**
     * Resolve a slug to its original URL and increment the click counter.
     *
     * @param slug the short URL slug
     * @return the original URL to redirect to
     * @throws ShortUrlNotFoundException if slug doesn't exist
     * @throws ShortUrlExpiredException  if link is expired, inactive, or max-clicked
     */
    @Transactional
    public String resolveAndTrack(String slug) {

        // ── Step 1: Try cache first ──
        Optional<CachedShortUrl> cached = urlCacheService.get(slug);

        if (cached.isPresent()) {
            return handleCacheHit(cached.get(), slug);
        }

        // ── Step 2: Cache miss — fall through to database ──
        return handleCacheMiss(slug);
    }

    /**
     * Get link preview without redirecting or counting a click.
     * Preview always goes to DB (no caching needed for preview).
     */
    @Transactional(readOnly = true)
    public LinkPreviewResponse getPreview(String slug) {
        ShortUrl shortUrl = shortUrlRepository.findBySlug(slug)
            .orElseThrow(() -> new ShortUrlNotFoundException(slug));
        return shortUrlMapper.toPreview(shortUrl);
    }

    // ═══════════════════════════════════════════════════════
    //  CACHE HIT HANDLER
    // ═══════════════════════════════════════════════════════

    private String handleCacheHit(CachedShortUrl cached, String slug) {

        // Validate accessibility from cached data
        if (!cached.isAccessible()) {
            log.info("Cache HIT but URL is inaccessible for slug '{}' — evicting", slug);
            urlCacheService.evict(slug);

            // Also update DB to deactivate
            shortUrlRepository.findBySlug(slug).ifPresent(entity -> {
                entity.setActive(false);
                shortUrlRepository.save(entity);
            });

            throw new ShortUrlExpiredException(slug);
        }

        // Increment click count in DB (authoritative counter)
        shortUrlRepository.incrementClickCount(cached.id());

        // TODO Phase 4: Publish async click event for analytics
        //   clickEventPublisher.publish(new ClickEventPayload(slug, ip, userAgent, referer));

        log.debug("Cache HIT — Resolved slug '{}' → '{}'", slug, cached.originalUrl());
        return cached.originalUrl();
    }

    // ═══════════════════════════════════════════════════════
    //  CACHE MISS HANDLER
    // ═══════════════════════════════════════════════════════

    private String handleCacheMiss(String slug) {
        // Query database
        ShortUrl shortUrl = shortUrlRepository.findBySlug(slug).orElseThrow(() -> new ShortUrlNotFoundException(slug));

        // Check accessibility
        if (!shortUrl.isAccessible()) {
            handleInaccessibleUrl(shortUrl, slug);
        }

        // Populate cache (only if cacheable)
        if (CachedShortUrl.isCacheable(shortUrl)) {
            CachedShortUrl cached = CachedShortUrl.from(shortUrl);
            urlCacheService.put(slug, cached);
            log.debug("Cache MISS — Populated cache for slug '{}'", slug);
        } else {
            log.debug("Cache MISS — Slug '{}' is not cacheable (has maxClicks)", slug);
        }

        // Increment click count in DB
        shortUrlRepository.incrementClickCount(shortUrl.getId());

        // TODO Phase 4: Publish async click event

        log.debug("Cache MISS — Resolved slug '{}' → '{}'", slug, shortUrl.getOriginalUrl());
        return shortUrl.getOriginalUrl();
    }

    // ═══════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════

    /**
     * Handle an inaccessible URL — deactivate it in the DB and throw.
     */
    private void handleInaccessibleUrl(ShortUrl shortUrl, String slug) {
        if (!shortUrl.isActive()) {
            throw new ShortUrlExpiredException(slug);
        }

        if (shortUrl.isExpired()) {
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
}
