package com.nazir.urlshortener.controller;

import com.nazir.urlshortener.dto.request.BulkCreateRequest;
import com.nazir.urlshortener.dto.request.CreateShortUrlRequest;
import com.nazir.urlshortener.dto.request.UpdateShortUrlRequest;
import com.nazir.urlshortener.dto.response.BulkCreateResponse;
import com.nazir.urlshortener.dto.response.ErrorResponse;
import com.nazir.urlshortener.dto.response.ShortUrlResponse;
import com.nazir.urlshortener.service.ShortUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST controller for URL shortening CRUD operations.
 * <p>
 * Base path: /api/v1/urls
 * </p>
 */
@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "Short URLs", description = "Create, read, update, and delete short URLs")
public class ShortUrlController {

    private static final Logger log = LoggerFactory.getLogger(ShortUrlController.class);

    private final ShortUrlService shortUrlService;

    public ShortUrlController(ShortUrlService shortUrlService) {
        this.shortUrlService = shortUrlService;
    }

    // ─── CREATE ──────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a short URL",
        description = "Shorten a URL with optional custom slug, expiration, password, and max clicks")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Short URL created"),
        @ApiResponse(responseCode = "400", description = "Invalid URL or request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Custom slug already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShortUrlResponse> create(
        @Valid @RequestBody CreateShortUrlRequest request) {

        log.info("POST /api/v1/urls — Creating short URL for: {}", request.url());
        ShortUrlResponse response = shortUrlService.create(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .location(URI.create(response.shortUrl()))
            .body(response);
    }

    // ─── READ (single) ──────────────────────────────────────

    @GetMapping("/{slug}")
    @Operation(summary = "Get short URL details",
        description = "Retrieve full details of a short URL by its slug")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Short URL found"),
        @ApiResponse(responseCode = "404", description = "Slug not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShortUrlResponse> getBySlug(
        @Parameter(description = "The short URL slug", example = "abc123")
        @PathVariable String slug) {

        ShortUrlResponse response = shortUrlService.getBySlug(slug);
        return ResponseEntity.ok(response);
    }

    // ─── READ (list) ─────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List short URLs",
        description = "Paginated list of short URLs with optional search. " +
            "In Phase 5, this will be filtered by authenticated user.")
    @ApiResponse(responseCode = "200", description = "Paginated list of URLs")
    public ResponseEntity<Page<ShortUrlResponse>> list(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable,

        @Parameter(description = "Search term (filters slug and original URL)")
        @RequestParam(required = false) String search) {

        Page<ShortUrlResponse> page = shortUrlService.list(pageable, search);
        return ResponseEntity.ok(page);
    }

    // ─── UPDATE ──────────────────────────────────────────────

    @PatchMapping("/{slug}")
    @Operation(summary = "Update a short URL",
        description = "Partially update a short URL. Only non-null fields are applied (PATCH semantics).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Short URL updated"),
        @ApiResponse(responseCode = "400", description = "Invalid update data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Slug not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ShortUrlResponse> update(
        @Parameter(description = "The short URL slug", example = "abc123")
        @PathVariable String slug,
        @Valid @RequestBody UpdateShortUrlRequest request) {

        log.info("PATCH /api/v1/urls/{} — Updating", slug);
        ShortUrlResponse response = shortUrlService.update(slug, request);
        return ResponseEntity.ok(response);
    }

    // ─── DELETE ──────────────────────────────────────────────

    @DeleteMapping("/{slug}")
    @Operation(summary = "Delete a short URL",
        description = "Permanently delete a short URL and all associated click data")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Short URL deleted"),
        @ApiResponse(responseCode = "404", description = "Slug not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "The short URL slug", example = "abc123")
        @PathVariable String slug) {

        log.info("DELETE /api/v1/urls/{}", slug);
        shortUrlService.delete(slug);
        return ResponseEntity.noContent().build();
    }

    // ─── BULK CREATE ─────────────────────────────────────────

    @PostMapping("/bulk")
    @Operation(summary = "Bulk create short URLs",
        description = "Create up to 100 short URLs in a single request. " +
            "Each URL is processed independently — individual failures don't affect others.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Bulk operation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BulkCreateResponse> bulkCreate(
        @Valid @RequestBody BulkCreateRequest request) {

        log.info("POST /api/v1/urls/bulk — Bulk creating {} URLs", request.urls().size());
        BulkCreateResponse response = shortUrlService.bulkCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
