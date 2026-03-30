package com.nazir.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Type-safe binding for all custom 'app.*' properties from application.yml.
 * Uses Java 21 records with constructor binding (immutable).
 * <p>
 * Maps to:
 * <pre>
 * app:
 *   base-url: http://localhost:8080
 *   slug:
 *     default-length: 7
 *     ...
 * </pre>
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String baseUrl, SlugProperties slug, CorsProperties cors,CacheProperties cache) {

    public record SlugProperties(int defaultLength, int minCustomLength, int maxCustomLength) {
        /**
         * Provide sensible defaults if not configured.
         */
        public SlugProperties {
            if (defaultLength <= 0) defaultLength = 7;
            if (minCustomLength <= 0) minCustomLength = 3;
            if (maxCustomLength <= 0) maxCustomLength = 20;
        }
    }

    public record CorsProperties(List<String> allowedOrigins) {
        public CorsProperties {
            if (allowedOrigins == null) allowedOrigins = List.of("http://localhost:3000");
        }
    }

    /**
     * Cache configuration properties.
     *
     * <pre>
     * app:
     *   cache:
     *     url-ttl-hours: 24
     *     analytics-ttl-minutes: 5
     *     key-prefix: "urlshortener:"
     * </pre>
     *
     * @param urlTtlHours         TTL for cached slug→URL mappings (default: 24)
     * @param analyticsTtlMinutes TTL for cached analytics responses (default: 5)
     * @param keyPrefix           Redis key prefix (default: "urlshortener:")
     */
    public record CacheProperties(int urlTtlHours, int analyticsTtlMinutes, String keyPrefix) {
        public CacheProperties {
            if (urlTtlHours <= 0) urlTtlHours = 24;
            if (analyticsTtlMinutes <= 0) analyticsTtlMinutes = 5;
            if (keyPrefix == null || keyPrefix.isBlank()) keyPrefix = "urlshortener:";
        }

        /**
         * Convenience method: URL cache TTL as Duration.
         */
        public Duration urlTtl() {
            return Duration.ofHours(urlTtlHours);
        }

        /**
         * Convenience method: Analytics cache TTL as Duration.
         */
        public Duration analyticsTtl() {
            return Duration.ofMinutes(analyticsTtlMinutes);
        }
    }


}
