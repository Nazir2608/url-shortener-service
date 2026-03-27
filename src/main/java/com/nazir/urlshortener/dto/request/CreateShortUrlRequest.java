package com.nazir.urlshortener.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Request payload for creating a new short URL.
 * <p>
 * Only {@code url} is required. All other fields are optional.
 * </p>
 *
 * @param url        the original URL to shorten (required)
 * @param customSlug optional custom slug (3-20 chars, alphanumeric + hyphens)
 * @param expiresAt  optional expiration timestamp (must be in the future)
 * @param password   optional password to protect the link
 * @param maxClicks  optional maximum number of clicks before deactivation
 * @param groupId    optional URL group UUID for organization
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Request to create a shortened URL")
public record CreateShortUrlRequest(

    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    @Schema(description = "Original URL to shorten",
        example = "https://www.example.com/very/long/path/to/resource?param=value")
    String url,

    @Size(min = 3, max = 20, message = "Custom slug must be 3-20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?$",
        message = "Slug must be alphanumeric with optional hyphens, cannot start/end with hyphen")
    @Schema(description = "Custom slug (optional). If omitted, one is auto-generated.",
        example = "my-brand")
    String customSlug,

    @Future(message = "Expiration date must be in the future")
    @Schema(description = "Expiration timestamp (optional)",
        example = "2025-12-31T23:59:59")
    LocalDateTime expiresAt,

    @Schema(description = "Password to protect the link (optional)")
    String password,

    @Min(value = 1, message = "Max clicks must be at least 1")
    @Schema(description = "Auto-deactivate after this many clicks (optional)",
        example = "1000")
    Integer maxClicks,

    @Schema(description = "Group/folder UUID to organize the link (optional)")
    String groupId
) {
}
