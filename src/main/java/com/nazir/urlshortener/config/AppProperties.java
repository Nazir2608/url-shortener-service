package com.nazir.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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
public record AppProperties(
    String baseUrl,
    SlugProperties slug,
    CorsProperties cors
) {

    public record SlugProperties(
        int defaultLength,
        int minCustomLength,
        int maxCustomLength
    ) {
        /**
         * Provide sensible defaults if not configured.
         */
        public SlugProperties {
            if (defaultLength <= 0) defaultLength = 7;
            if (minCustomLength <= 0) minCustomLength = 3;
            if (maxCustomLength <= 0) maxCustomLength = 20;
        }
    }

    public record CorsProperties(
        List<String> allowedOrigins
    ) {
        public CorsProperties {
            if (allowedOrigins == null) allowedOrigins = List.of("http://localhost:3000");
        }
    }
}
