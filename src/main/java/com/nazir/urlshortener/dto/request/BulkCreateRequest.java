package com.nazir.urlshortener.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request payload for creating multiple short URLs in one call.
 *
 * @param urls list of URL creation requests (1-100 items)
 */
@Schema(description = "Bulk create multiple short URLs")
public record BulkCreateRequest(

    @NotEmpty(message = "URL list must not be empty")
    @Size(max = 100, message = "Cannot create more than 100 URLs at once")
    @Valid
    List<CreateShortUrlRequest> urls
) {
}
