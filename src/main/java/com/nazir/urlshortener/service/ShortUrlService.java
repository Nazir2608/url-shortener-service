package com.nazir.urlshortener.service;

import com.nazir.urlshortener.domain.ShortUrl;
import com.nazir.urlshortener.dto.mapper.ShortUrlMapper;
import com.nazir.urlshortener.dto.request.BulkCreateRequest;
import com.nazir.urlshortener.dto.request.CreateShortUrlRequest;
import com.nazir.urlshortener.dto.request.UpdateShortUrlRequest;
import com.nazir.urlshortener.dto.response.BulkCreateResponse;
import com.nazir.urlshortener.dto.response.BulkCreateResponse.BulkItemResult;
import com.nazir.urlshortener.dto.response.ShortUrlResponse;
import com.nazir.urlshortener.exception.ShortUrlNotFoundException;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Core business logic for URL shortening operations.
 * <p>
 * Handles CRUD operations, slug generation, password hashing,
 * and input validation delegation.
 * </p>
 */
@Service
public class ShortUrlService {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlService.class);

    private final ShortUrlRepository shortUrlRepository;
    private final SlugGeneratorService slugGeneratorService;
    private final UrlValidationService urlValidationService;
    private final ShortUrlMapper shortUrlMapper;
    private final PasswordEncoder passwordEncoder;

    public ShortUrlService(ShortUrlRepository shortUrlRepository,
                           SlugGeneratorService slugGeneratorService,
                           UrlValidationService urlValidationService,
                           ShortUrlMapper shortUrlMapper,
                           PasswordEncoder passwordEncoder) {
        this.shortUrlRepository = shortUrlRepository;
        this.slugGeneratorService = slugGeneratorService;
        this.urlValidationService = urlValidationService;
        this.shortUrlMapper = shortUrlMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ═══════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════

    /**
     * Create a new short URL.
     *
     * @param request creation request with URL and optional parameters
     * @return the created short URL response
     */
    @Transactional
    public ShortUrlResponse create(CreateShortUrlRequest request) {
        log.info("Creating short URL for: {}", request.url());

        // 1. Validate and normalize the original URL
        String validatedUrl = urlValidationService.validateAndNormalize(request.url());

        // 2. Generate or validate slug
        String slug = (request.customSlug() != null && !request.customSlug().isBlank())
            ? slugGeneratorService.validateCustomSlug(request.customSlug())
            : slugGeneratorService.generateUniqueSlug();

        // 3. Build entity
        ShortUrl shortUrl = ShortUrl.builder()
            .slug(slug)
            .originalUrl(validatedUrl)
            .expiresAt(request.expiresAt())
            .maxClicks(request.maxClicks())
            .build();

        // 4. Hash password if provided
        if (request.password() != null && !request.password().isBlank()) {
            shortUrl.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        // 5. Save
        ShortUrl saved = shortUrlRepository.save(shortUrl);
        log.info("Short URL created: {} → {}", saved.getSlug(), saved.getOriginalUrl());

        return shortUrlMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════
    //  READ
    // ═══════════════════════════════════════════════════════

    /**
     * Get a short URL by its slug.
     *
     * @param slug the slug to look up
     * @return short URL response
     * @throws ShortUrlNotFoundException if slug doesn't exist
     */
    @Transactional(readOnly = true)
    public ShortUrlResponse getBySlug(String slug) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        return shortUrlMapper.toResponse(shortUrl);
    }

    /**
     * List all short URLs with pagination.
     * <p>
     * Note: In Phase 5, this will be filtered by authenticated user.
     * </p>
     *
     * @param pageable pagination parameters
     * @param search   optional search term (searches slug and original URL)
     * @return paginated list of short URL responses
     */
    @Transactional(readOnly = true)
    public Page<ShortUrlResponse> list(Pageable pageable, String search) {
        Page<ShortUrl> page;

        if (search != null && !search.isBlank()) {
            // TODO Phase 5: Add userId filter — searchByUser(userId, search, pageable)
            page = shortUrlRepository.findAll(pageable);
            // For now, basic search isn't filtered by user
            // This will be properly implemented with auth
        } else {
            // TODO Phase 5: Filter by userId — findByUserId(userId, pageable)
            page = shortUrlRepository.findAll(pageable);
        }

        return shortUrlMapper.toResponsePage(page);
    }

    // ═══════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════

    /**
     * Partially update a short URL.
     * Only non-null fields in the request are applied.
     *
     * @param slug    the slug of the URL to update
     * @param request update request with optional fields
     * @return updated short URL response
     * @throws ShortUrlNotFoundException if slug doesn't exist
     */
    @Transactional
    public ShortUrlResponse update(String slug, UpdateShortUrlRequest request) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);

        // Apply non-null fields (PATCH semantics)
        if (request.originalUrl() != null) {
            String validatedUrl = urlValidationService.validateAndNormalize(request.originalUrl());
            shortUrl.setOriginalUrl(validatedUrl);
        }

        if (request.isActive() != null) {
            shortUrl.setActive(request.isActive());
        }

        if (request.expiresAt() != null) {
            shortUrl.setExpiresAt(request.expiresAt());
        }

        if (request.maxClicks() != null) {
            shortUrl.setMaxClicks(request.maxClicks());
        }

        ShortUrl updated = shortUrlRepository.save(shortUrl);
        log.info("Short URL updated: {}", slug);

        return shortUrlMapper.toResponse(updated);
    }

    // ═══════════════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════════════

    /**
     * Delete a short URL by slug.
     *
     * @param slug the slug to delete
     * @throws ShortUrlNotFoundException if slug doesn't exist
     */
    @Transactional
    public void delete(String slug) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        shortUrlRepository.delete(shortUrl);
        log.info("Short URL deleted: {}", slug);
    }

    // ═══════════════════════════════════════════════════════
    //  BULK CREATE
    // ═══════════════════════════════════════════════════════

    /**
     * Create multiple short URLs in one operation.
     * Each URL is processed independently — failures don't affect other items.
     *
     * @param request bulk creation request
     * @return results for each URL (success or failure)
     */
    @Transactional
    public BulkCreateResponse bulkCreate(BulkCreateRequest request) {
        log.info("Bulk creating {} URLs", request.urls().size());

        List<BulkItemResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (int i = 0; i < request.urls().size(); i++) {
            CreateShortUrlRequest urlRequest = request.urls().get(i);
            try {
                ShortUrlResponse response = create(urlRequest);
                results.add(BulkItemResult.success(i, response));
                successCount++;
            } catch (Exception e) {
                results.add(BulkItemResult.failure(i, e.getMessage()));
                failureCount++;
                log.warn("Bulk create failed for index {}: {}", i, e.getMessage());
            }
        }

        log.info("Bulk create complete: {}/{} succeeded",
            successCount, request.urls().size());

        return new BulkCreateResponse(
            request.urls().size(),
            successCount,
            failureCount,
            results
        );
    }

    // ═══════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════

    private ShortUrl findBySlugOrThrow(String slug) {
        return shortUrlRepository.findBySlug(slug)
            .orElseThrow(() -> new ShortUrlNotFoundException(slug));
    }
}
