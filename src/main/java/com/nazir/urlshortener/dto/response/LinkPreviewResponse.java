package com.nazir.urlshortener.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response for link preview (/{slug}+ endpoint).
 * Shows destination info without triggering a redirect or counting a click.
 */
@Schema(description = "Preview of a short URL destination (no redirect, no click counted)")
public record LinkPreviewResponse(

    @Schema(description = "Short slug", example = "abc123")
    String slug,

    @Schema(description = "Full short URL", example = "http://localhost:8080/abc123")
    String shortUrl,

    @Schema(description = "Original destination URL",
        example = "https://www.example.com/very/long/path")
    String originalUrl,

    @Schema(description = "Whether the link requires a password", example = "false")
    boolean isPasswordProtected,

    @Schema(description = "When the link was created")
    LocalDateTime createdAt,

    @Schema(description = "Total click count", example = "42")
    long clickCount
) {
}
