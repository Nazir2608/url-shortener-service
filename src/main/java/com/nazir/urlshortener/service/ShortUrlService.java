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
 * Core business logic for URL shortening CRUD operations.
 * <p>
 * Phase 3: Added cache eviction on all mutation operations
 * (create is not cached — only populated on first redirect).
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
    private final UrlCacheService urlCacheService;                     // ← NEW

    public ShortUrlService(ShortUrlRepository shortUrlRepository,
                           SlugGeneratorService slugGeneratorService,
                           UrlValidationService urlValidationService,
                           ShortUrlMapper shortUrlMapper,
                           PasswordEncoder passwordEncoder,
                           UrlCacheService urlCacheService) {          // ← NEW
        this.shortUrlRepository = shortUrlRepository;
        this.slugGeneratorService = slugGeneratorService;
        this.urlValidationService = urlValidationService;
        this.shortUrlMapper = shortUrlMapper;
        this.passwordEncoder = passwordEncoder;
        this.urlCacheService = urlCacheService;                         // ← NEW
    }

    // ═══════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════

    @Transactional
    public ShortUrlResponse create(CreateShortUrlRequest request) {
        log.info("Creating short URL for: {}", request.url());

        String validatedUrl = urlValidationService.validateAndNormalize(request.url());

        String slug = (request.customSlug() != null && !request.customSlug().isBlank())
            ? slugGeneratorService.validateCustomSlug(request.customSlug())
            : slugGeneratorService.generateUniqueSlug();

        ShortUrl shortUrl = ShortUrl.builder()
            .slug(slug)
            .originalUrl(validatedUrl)
            .expiresAt(request.expiresAt())
            .maxClicks(request.maxClicks())
            .build();

        if (request.password() != null && !request.password().isBlank()) {
            shortUrl.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        ShortUrl saved = shortUrlRepository.save(shortUrl);
        log.info("Short URL created: {} → {}", saved.getSlug(), saved.getOriginalUrl());

        // Note: We do NOT pre-populate cache on create.
        // Cache is populated on first redirect (cache-aside / lazy-loading).

        return shortUrlMapper.toResponse(saved);
    }

    // ═══════════════════════════════════════════════════════
    //  READ
    // ═══════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public ShortUrlResponse getBySlug(String slug) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        return shortUrlMapper.toResponse(shortUrl);
    }

    @Transactional(readOnly = true)
    public Page<ShortUrlResponse> list(Pageable pageable, String search) {
        Page<ShortUrl> page;

        if (search != null && !search.isBlank()) {
            page = shortUrlRepository.findAll(pageable);
        } else {
            page = shortUrlRepository.findAll(pageable);
        }

        return shortUrlMapper.toResponsePage(page);
    }

    // ═══════════════════════════════════════════════════════
    //  UPDATE — with cache eviction
    // ═══════════════════════════════════════════════════════

    @Transactional
    public ShortUrlResponse update(String slug, UpdateShortUrlRequest request) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);

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

        // ── Phase 3: Evict stale cache entry ──
        urlCacheService.evict(slug);
        log.info("Short URL updated and cache evicted: {}", slug);

        return shortUrlMapper.toResponse(updated);
    }

    // ═══════════════════════════════════════════════════════
    //  DELETE — with cache eviction
    // ═══════════════════════════════════════════════════════

    @Transactional
    public void delete(String slug) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        shortUrlRepository.delete(shortUrl);

        // ── Phase 3: Evict cache entry ──
        urlCacheService.evict(slug);
        log.info("Short URL deleted and cache evicted: {}", slug);
    }

    // ═══════════════════════════════════════════════════════
    //  BULK CREATE
    // ═══════════════════════════════════════════════════════

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
