package com.nazir.urlshortener.service;
import com.nazir.urlshortener.domain.ShortUrl;
import com.nazir.urlshortener.exception.ShortUrlExpiredException;
import com.nazir.urlshortener.exception.ShortUrlNotFoundException;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RedirectService {

    private static final Logger log = LoggerFactory.getLogger(RedirectService.class);
    private static final String CACHE_PREFIX = "url:";
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final String SEPARATOR = "|";

    private final ShortUrlRepository shortUrlRepository;
    private final StringRedisTemplate redisTemplate;

    public RedirectService(ShortUrlRepository shortUrlRepository,
                           StringRedisTemplate redisTemplate) {
        this.shortUrlRepository = shortUrlRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Resolves a slug to its original URL + ID.
     *
     * Strategy: Cache-aside (read-through)
     *   1. Check Redis → cache HIT → return
     *   2. Cache MISS → query DB → validate → cache → return
     *
     * @return ResolvedUrl with both ID (for click tracking) and original URL
     */
    public ResolvedUrl resolve(String slug) {
        String cacheKey = CACHE_PREFIX + slug;

        // 1. Try cache
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache HIT for slug='{}'", slug);
            return parseCachedValue(cached, slug);
        }

        // 2. Cache miss → DB
        log.debug("Cache MISS for slug='{}', querying DB", slug);
        ShortUrl shortUrl = shortUrlRepository.findBySlug(slug)
            .orElseThrow(() -> new ShortUrlNotFoundException(
                "Short URL not found: " + slug));

        // 3. Validate accessibility
        if (!shortUrl.isAccessible()) {
            throw new ShortUrlExpiredException(
                "This link has expired or been deactivated");
        }

        // 4. Cache the result: "uuid|originalUrl"
        String cacheValue = shortUrl.getId() + SEPARATOR + shortUrl.getOriginalUrl();
        redisTemplate.opsForValue().set(cacheKey, cacheValue, CACHE_TTL);

        return new ResolvedUrl(shortUrl.getId(), shortUrl.getOriginalUrl());
    }

    /**
     * Evicts a slug from cache. Call on URL update or delete.
     */
    public void evictCache(String slug) {
        String cacheKey = CACHE_PREFIX + slug;
        redisTemplate.delete(cacheKey);
        log.debug("Cache evicted for slug='{}'", slug);
    }

    private ResolvedUrl parseCachedValue(String cached, String slug) {
        int idx = cached.indexOf(SEPARATOR);
        if (idx == -1) {
            // Old cache format without ID — evict and re-resolve
            evictCache(slug);
            return resolve(slug);
        }
        UUID id = UUID.fromString(cached.substring(0, idx));
        String originalUrl = cached.substring(idx + 1);
        return new ResolvedUrl(id, originalUrl);
    }

    /**
     * Result record — holds everything the redirect controller needs.
     */
    public record ResolvedUrl(UUID id, String originalUrl) {}
}
