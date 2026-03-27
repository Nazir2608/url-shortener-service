package com.nazir.urlshortener.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request payload for updating an existing short URL.
 * <p>
 * All fields are optional. Only non-null fields will be applied.
 * </p>
 *
 * @param originalUrl new destination URL (null = keep current)
 * @param isActive    activate/deactivate the link (null = keep current)
 * @param expiresAt   new expiration timestamp (null = keep current)
 * @param maxClicks   new max click limit (null = keep current)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Partial update for a short URL. Only non-null fields are applied.")
public record UpdateShortUrlRequest(

    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    @Schema(description = "New destination URL",
        example = "https://www.updated-example.com/new-path")
    String originalUrl,

    @Schema(description = "Activate or deactivate the link",
        example = "false")
    Boolean isActive,

    @Future(message = "Expiration date must be in the future")
    @Schema(description = "New expiration timestamp",
        example = "2026-06-30T23:59:59")
    LocalDateTime expiresAt,

    @Min(value = 1, message = "Max clicks must be at least 1")
    @Schema(description = "New maximum click count",
        example = "5000")
    Integer maxClicks
) {
}
