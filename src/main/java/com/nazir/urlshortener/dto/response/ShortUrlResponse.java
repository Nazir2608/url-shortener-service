package com.nazir.urlshortener.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response payload representing a short URL.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Short URL details")
public record ShortUrlResponse(

    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Short slug", example = "abc123")
    String slug,

    @Schema(description = "Full short URL", example = "http://localhost:8080/abc123")
    String shortUrl,

    @Schema(description = "Original destination URL",
        example = "https://www.example.com/very/long/path")
    String originalUrl,

    @Schema(description = "Whether the link is active", example = "true")
    boolean isActive,

    @Schema(description = "Whether the link is password-protected", example = "false")
    boolean isPasswordProtected,

    @Schema(description = "Expiration timestamp")
    LocalDateTime expiresAt,

    @Schema(description = "Maximum clicks before auto-deactivation")
    Integer maxClicks,

    @Schema(description = "Total click count", example = "42")
    long clickCount,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Last update timestamp")
    LocalDateTime updatedAt
) {
}
