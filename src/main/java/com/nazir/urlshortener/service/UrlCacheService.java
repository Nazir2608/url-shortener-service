package com.nazir.urlshortener.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nazir.urlshortener.config.AppProperties;
import com.nazir.urlshortener.dto.cache.CachedShortUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis-backed cache for URL slug → original URL resolution.
 * <p>
 * Uses the <b>cache-aside</b> (lazy-loading) pattern:
 * <ol>
 *   <li>Check cache on read</li>
 *   <li>On miss → load from DB → populate cache</li>
 *   <li>On write (update/delete) → evict cache entry</li>
 * </ol>
 *
 * <h3>Resilience:</h3>
 * All Redis operations are wrapped in try-catch. If Redis is unavailable,
 * the application falls through to the database and continues working.
 * Cache failures are logged as warnings, never thrown to callers.
 *
 * <h3>Key format:</h3>
 * {@code {prefix}url:{slug}} → e.g., {@code urlshortener:url:abc123}
 */
@Service
public class UrlCacheService {

    private static final Logger log = LoggerFactory.getLogger(UrlCacheService.class);
    private static final String URL_KEY_SEGMENT = "url:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    // Simple hit/miss counters for observability (Phase 8: replace with Micrometer)
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public UrlCacheService(StringRedisTemplate redisTemplate, @Qualifier("cacheObjectMapper") ObjectMapper objectMapper, AppProperties appProperties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    // ═══════════════════════════════════════════════════════
    //  GET — Cache Lookup
    // ═══════════════════════════════════════════════════════

    /**
     * Look up a cached URL by slug.
     *
     * @param slug the short URL slug
     * @return cached URL data, or empty if not in cache or Redis is unavailable
     */
    public Optional<CachedShortUrl> get(String slug) {
        if (slug == null || slug.isBlank()) {
            return Optional.empty();
        }

        try {
            String json = redisTemplate.opsForValue().get(buildKey(slug));

            if (json == null) {
                cacheMisses.incrementAndGet();
                log.debug("Cache MISS for slug '{}'", slug);
                return Optional.empty();
            }

            CachedShortUrl cached = objectMapper.readValue(json, CachedShortUrl.class);
            cacheHits.incrementAndGet();
            log.debug("Cache HIT for slug '{}'", slug);
            return Optional.of(cached);

        } catch (JsonProcessingException e) {
            // Corrupted cache entry — evict it and treat as miss
            log.warn("Corrupted cache entry for slug '{}', evicting: {}", slug, e.getMessage());
            evict(slug);
            cacheMisses.incrementAndGet();
            return Optional.empty();

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis unavailable during GET for slug '{}': {}", slug, e.getMessage());
            cacheMisses.incrementAndGet();
            return Optional.empty();

        } catch (Exception e) {
            log.warn("Unexpected cache error during GET for slug '{}': {}", slug, e.getMessage());
            cacheMisses.incrementAndGet();
            return Optional.empty();
        }
    }

    // ═══════════════════════════════════════════════════════
    //  PUT — Cache Population
    // ═══════════════════════════════════════════════════════

    /**
     * Store a URL in the cache with the configured TTL.
     * <p>
     * If the URL has an expiration time, the cache TTL is set to
     * the minimum of the configured TTL and the time until expiration.
     * This prevents serving stale cached data for expired URLs.
     * </p>
     *
     * @param slug   the short URL slug
     * @param cached the cached URL data
     */
    public void put(String slug, CachedShortUrl cached) {
        if (slug == null || cached == null) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(cached);
            Duration ttl = calculateTtl(cached);

            if (ttl.isZero() || ttl.isNegative()) {
                log.debug("Skipping cache for slug '{}' — TTL is zero/negative", slug);
                return;
            }

            redisTemplate.opsForValue().set(buildKey(slug), json, ttl);
            log.debug("Cached slug '{}' with TTL {}s", slug, ttl.toSeconds());

        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize cache entry for slug '{}': {}", slug, e.getMessage());

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis unavailable during PUT for slug '{}': {}", slug, e.getMessage());

        } catch (Exception e) {
            log.warn("Unexpected cache error during PUT for slug '{}': {}", slug, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  EVICT — Cache Invalidation
    // ═══════════════════════════════════════════════════════

    /**
     * Remove a URL from the cache.
     * Called on update, delete, or when stale data is detected.
     *
     * @param slug the short URL slug to evict
     */
    public void evict(String slug) {
        if (slug == null || slug.isBlank()) {
            return;
        }

        try {
            Boolean deleted = redisTemplate.delete(buildKey(slug));
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Evicted cache for slug '{}'", slug);
            }

        } catch (RedisConnectionFailureException e) {
            log.warn("Redis unavailable during EVICT for slug '{}': {}", slug, e.getMessage());

        } catch (Exception e) {
            log.warn("Unexpected cache error during EVICT for slug '{}': {}", slug, e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    //  STATS — Observability
    // ═══════════════════════════════════════════════════════

    /**
     * Get the current cache hit count.
     * Phase 8 will replace this with Micrometer counters.
     */
    public long getCacheHits() {
        return cacheHits.get();
    }

    /**
     * Get the current cache miss count.
     */
    public long getCacheMisses() {
        return cacheMisses.get();
    }

    /**
     * Get the hit ratio as a percentage (0.0 – 100.0).
     */
    public double getHitRatio() {
        long hits = cacheHits.get();
        long total = hits + cacheMisses.get();
        return total == 0 ? 0.0 : (hits * 100.0) / total;
    }

    // ═══════════════════════════════════════════════════════
    //  INTERNAL HELPERS
    // ═══════════════════════════════════════════════════════

    /**
     * Build the Redis key for a given slug.
     *
     * @return e.g., "urlshortener:url:abc123"
     */
    String buildKey(String slug) {
        return appProperties.cache().keyPrefix() + URL_KEY_SEGMENT + slug;
    }

    /**
     * Calculate the cache TTL for a given cached URL.
     * <p>
     * If the URL has an expiration time, the TTL is the minimum of:
     * <ul>
     *   <li>The configured default TTL ({@code app.cache.url-ttl-hours})</li>
     *   <li>The time remaining until URL expiration</li>
     * </ul>
     * This ensures the cache entry expires before or when the URL expires.
     * </p>
     */
    private Duration calculateTtl(CachedShortUrl cached) {
        Duration defaultTtl = appProperties.cache().urlTtl();

        if (cached.expiresAt() == null) {
            return defaultTtl;
        }

        Duration timeUntilExpiry = Duration.between(LocalDateTime.now(), cached.expiresAt());

        if (timeUntilExpiry.isNegative() || timeUntilExpiry.isZero()) {
            return Duration.ZERO;
        }

        // Use the shorter of the two durations
        return timeUntilExpiry.compareTo(defaultTtl) < 0 ? timeUntilExpiry : defaultTtl;
    }
}
