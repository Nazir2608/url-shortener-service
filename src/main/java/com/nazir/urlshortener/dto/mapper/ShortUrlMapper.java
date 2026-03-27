package com.nazir.urlshortener.dto.mapper;

import com.nazir.urlshortener.config.AppProperties;
import com.nazir.urlshortener.domain.ShortUrl;
import com.nazir.urlshortener.dto.response.LinkPreviewResponse;
import com.nazir.urlshortener.dto.response.ShortUrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Maps ShortUrl entities to response DTOs.
 * <p>
 * Hand-written instead of MapStruct because:
 * <ul>
 *   <li>{@code shortUrl} field is computed (baseUrl + "/" + slug)</li>
 *   <li>{@code isPasswordProtected} is a derived property</li>
 *   <li>Records make manual mapping clean and explicit</li>
 * </ul>
 */
@Component
public class ShortUrlMapper {

    private final String baseUrl;

    public ShortUrlMapper(AppProperties appProperties) {
        this.baseUrl = appProperties.baseUrl();
    }

    /**
     * Convert a ShortUrl entity to a full response DTO.
     */
    public ShortUrlResponse toResponse(ShortUrl entity) {
        if (entity == null) return null;

        return new ShortUrlResponse(
            entity.getId(),
            entity.getSlug(),
            buildShortUrl(entity.getSlug()),
            entity.getOriginalUrl(),
            entity.isActive(),
            entity.isPasswordProtected(),
            entity.getExpiresAt(),
            entity.getMaxClicks(),
            entity.getClickCount(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Convert a page of ShortUrl entities to a page of response DTOs.
     * Spring's Page.map() preserves pagination metadata.
     */
    public Page<ShortUrlResponse> toResponsePage(Page<ShortUrl> page) {
        return page.map(this::toResponse);
    }

    /**
     * Convert a ShortUrl entity to a link preview DTO.
     * Exposes minimal information — no update timestamps, no active status.
     */
    public LinkPreviewResponse toPreview(ShortUrl entity) {
        if (entity == null) return null;

        return new LinkPreviewResponse(
            entity.getSlug(),
            buildShortUrl(entity.getSlug()),
            entity.getOriginalUrl(),
            entity.isPasswordProtected(),
            entity.getCreatedAt(),
            entity.getClickCount()
        );
    }

    private String buildShortUrl(String slug) {
        return baseUrl + "/" + slug;
    }
}
