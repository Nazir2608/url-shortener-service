package com.nazir.urlshortener.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response for bulk URL creation.
 * Each item in results is either a success (with shortUrl) or failure (with error).
 */
@Schema(description = "Results of bulk URL creation")
public record BulkCreateResponse(

    @Schema(description = "Total URLs requested", example = "3")
    int totalRequested,

    @Schema(description = "Successfully created count", example = "2")
    int successCount,

    @Schema(description = "Failed count", example = "1")
    int failureCount,

    List<BulkItemResult> results
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Individual result for a bulk create item")
    public record BulkItemResult(

        @Schema(description = "Index in the original request array", example = "0")
        int index,

        @Schema(description = "Whether this item was created successfully", example = "true")
        boolean success,

        @Schema(description = "Created short URL (present on success)")
        ShortUrlResponse shortUrl,

        @Schema(description = "Error message (present on failure)")
        String error
    ) {
        public static BulkItemResult success(int index, ShortUrlResponse shortUrl) {
            return new BulkItemResult(index, true, shortUrl, null);
        }

        public static BulkItemResult failure(int index, String error) {
            return new BulkItemResult(index, false, null, error);
        }
    }
}
