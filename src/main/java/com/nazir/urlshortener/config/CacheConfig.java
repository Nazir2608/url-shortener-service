package com.nazir.urlshortener.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache-specific configuration.
 * <p>
 * Provides a dedicated {@link ObjectMapper} for Redis cache serialization,
 * separate from the web-layer ObjectMapper. This isolation prevents
 * cache breakage if the web ObjectMapper config changes.
 * </p>
 *
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li><b>Manual cache (not @Cacheable)</b> — gives full control over
 *       conditional caching, per-entry TTL, and error handling</li>
 *   <li><b>Write-invalidate strategy</b> — evict on mutation, repopulate on next read.
 *       Simpler and safer than write-through.</li>
 *   <li><b>StringRedisTemplate + manual JSON</b> — avoids {@code @class} type metadata
 *       pollution from GenericJackson2JsonRedisSerializer</li>
 * </ul>
 */
@Configuration
public class CacheConfig {

    /**
     * Dedicated ObjectMapper for cache serialization/deserialization.
     * <p>
     * Separate from the web ObjectMapper so that changes to web JSON config
     * (e.g., adding mixins, changing naming strategy) don't break cached data.
     * </p>
     */
    @Bean
    @Qualifier("cacheObjectMapper")
    public ObjectMapper cacheObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
