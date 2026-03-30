package com.nazir.urlshortener.dto.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.nazir.urlshortener.domain.ShortUrl;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight cached representation of a {@link ShortUrl} entity.
 * <p>
 * Stored in Redis as JSON. Contains only the fields needed for:
 * <ul>
 *   <li>Redirect resolution ({@code originalUrl})</li>
 *   <li>Accessibility checks ({@code active}, {@code expiresAt})</li>
 *   <li>Click count increment ({@code id})</li>
 * </ul>
 * </p>
 *
 * <h3>What is NOT cached:</h3>
 * <ul>
 *   <li>URLs with {@code maxClicks} — need real-time DB count check</li>
 *   <li>Click events — always written to DB</li>
 *   <li>User associations — not needed for redirect</li>
 * </ul>
 *
 * @param id                entity UUID (for DB click increment)
 * @param slug              the short slug
 * @param originalUrl       destination URL
 * @param active            whether the link is active
 * @param expiresAt         expiration timestamp (null = never)
 * @param passwordProtected whether a password is required
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CachedShortUrl(UUID id, String slug, String originalUrl, boolean active,
    LocalDateTime expiresAt, boolean passwordProtected) implements Serializable {

    /**
     * Factory method to create a cached representation from a JPA entity.
     */
    public static CachedShortUrl from(ShortUrl entity) {
        return new CachedShortUrl(
            entity.getId(),
            entity.getSlug(),
            entity.getOriginalUrl(),
            entity.isActive(),
            entity.getExpiresAt(),
            entity.isPasswordProtected()
        );
    }

    /**
     * Check if the cached URL has expired based on current time.
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the cached URL is accessible for redirect.
     * <p>
     * Note: {@code maxClicks} check is NOT here because URLs with
     * maxClicks are never cached (they need real-time DB count).
     * </p>
     */
    public boolean isAccessible() {
        return active && !isExpired();
    }

    /**
     * Determine if a ShortUrl entity should be cached.
     * <p>
     * URLs with {@code maxClicks} are excluded because each redirect
     * must check the current count in the database.
     * </p>
     */
    public static boolean isCacheable(ShortUrl entity) {
        return entity.isAccessible() && entity.getMaxClicks() == null;
    }
}
