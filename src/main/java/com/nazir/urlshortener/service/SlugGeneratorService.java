package com.nazir.urlshortener.service;

import com.nazir.urlshortener.config.AppProperties;
import com.nazir.urlshortener.exception.SlugAlreadyExistsException;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import com.nazir.urlshortener.util.Base62Encoder;
import com.nazir.urlshortener.util.SlugValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Generates and validates URL slugs.
 * <p>
 * Two strategies:
 * <ul>
 *   <li><b>Auto-generated:</b> Random Base62 string (default 7 chars)</li>
 *   <li><b>Custom:</b> User-provided slug with validation</li>
 * </ul>
 * <p>
 * Auto-generated slugs are retried up to {@value MAX_RETRIES} times on collision.
 * Custom slugs fail immediately if already taken.
 */
@Service
public class SlugGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(SlugGeneratorService.class);
    private static final int MAX_RETRIES = 5;

    private final ShortUrlRepository shortUrlRepository;
    private final AppProperties appProperties;

    public SlugGeneratorService(ShortUrlRepository shortUrlRepository, AppProperties appProperties) {
        this.shortUrlRepository = shortUrlRepository;
        this.appProperties = appProperties;
    }

    /**
     * Generate a unique random slug.
     * Retries up to 5 times if a collision occurs (extremely unlikely with 7 chars).
     *
     * @return a unique slug not present in the database
     * @throws RuntimeException if unable to generate unique slug after max retries
     */
    public String generateUniqueSlug() {
        int length = appProperties.slug().defaultLength();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            String slug = Base62Encoder.generateRandom(length);

            if (!shortUrlRepository.existsBySlug(slug) && !SlugValidator.isReserved(slug)) {
                log.debug("Generated unique slug '{}' on attempt {}", slug, attempt);
                return slug;
            }

            log.warn("Slug collision on attempt {} for slug '{}'", attempt, slug);

            // Increase length on each retry to reduce collision probability
            if (attempt >= 3) {
                length++;
            }
        }

        throw new RuntimeException("Failed to generate unique slug after %d attempts. This is extremely unlikely.".formatted(MAX_RETRIES));
    }

    /**
     * Validate a custom slug provided by the user.
     *
     * @param customSlug the user-provided slug
     * @return the validated slug (trimmed)
     * @throws com.nazir.urlshortener.exception.InvalidUrlException if format is invalid
     * @throws SlugAlreadyExistsException                           if slug is already taken
     */
    public String validateCustomSlug(String customSlug) {
        String slug = customSlug.trim();

        SlugValidator.ValidationResult result = SlugValidator.validate(
            slug,
            appProperties.slug().minCustomLength(),
            appProperties.slug().maxCustomLength()
        );

        if (!result.isValid()) {
            throw new com.nazir.urlshortener.exception.InvalidUrlException(
                slug, result.errorMessage());
        }

        if (shortUrlRepository.existsBySlug(slug)) {
            throw new SlugAlreadyExistsException(slug);
        }

        log.debug("Custom slug '{}' validated successfully", slug);
        return slug;
    }
}
