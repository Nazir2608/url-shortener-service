package com.nazir.urlshortener.controller;

import com.nazir.urlshortener.dto.response.LinkPreviewResponse;
import com.nazir.urlshortener.service.RedirectService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Handles short URL redirection — the HOT PATH.
 * <p>
 * Routes:
 * <ul>
 *   <li>{@code GET /{slug}} → 302 redirect to original URL</li>
 *   <li>{@code GET /{slug}+} → 200 link preview (no redirect, no click counted)</li>
 * </ul>
 * <p>
 * Performance target: &lt; 50ms (cache hit), &lt; 100ms (cache miss).
 * </p>
 */
@RestController
@Tag(name = "Redirect", description = "Short URL redirection and preview")
public class RedirectController {

    private static final Logger log = LoggerFactory.getLogger(RedirectController.class);

    private final RedirectService redirectService;

    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }

    /**
     * Redirect or preview a short URL.
     * <p>
     * If the slug ends with '+', returns a preview instead of redirecting.
     * Otherwise, performs a 302 redirect and counts a click.
     * </p>
     *
     * @param slug the short URL slug (optionally ending with '+' for preview)
     * @return 302 redirect or 200 preview
     */
    @GetMapping("/{slug}")
    @Operation(summary = "Redirect to original URL",
        description = "Resolves the slug and redirects (302) to the original URL. " +
            "Append '+' to the slug for a preview without redirecting.")
    @ApiResponses({
        @ApiResponse(responseCode = "302", description = "Redirect to original URL"),
        @ApiResponse(responseCode = "200", description = "Link preview (when slug ends with '+')"),
        @ApiResponse(responseCode = "404", description = "Slug not found"),
        @ApiResponse(responseCode = "410", description = "Link has expired or is deactivated")
    })
    public ResponseEntity<?> handleSlug(
        @Parameter(description = "Short URL slug. Append '+' for preview.",
            example = "abc123")
        @PathVariable String slug) {

        // ── Preview mode: slug ends with '+' ──
        if (slug.endsWith("+")) {
            String actualSlug = slug.substring(0, slug.length() - 1);
            log.debug("Preview request for slug: {}", actualSlug);
            LinkPreviewResponse preview = redirectService.getPreview(actualSlug);
            return ResponseEntity.ok(preview);
        }

        // ── Redirect mode ──
        log.debug("Redirect request for slug: {}", slug);
        String originalUrl = redirectService.resolveAndTrack(slug);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        // Cache-Control: prevent browsers from caching the redirect
        // so each visit is tracked
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        headers.setPragma("no-cache");

        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302
    }

    /**
     * Explicit preview endpoint (alternative to /{slug}+).
     * <p>
     * This endpoint is hidden from Swagger to avoid confusion
     * with the /{slug}+ pattern.
     */
    @Hidden
    @GetMapping("/api/v1/urls/{slug}/preview")
    public ResponseEntity<LinkPreviewResponse> preview(
        @PathVariable String slug) {

        LinkPreviewResponse preview = redirectService.getPreview(slug);
        return ResponseEntity.ok(preview);
    }
}
