package com.nazir.urlshortener.dto.response;

import java.util.List;

public record ReferrerAnalyticsResponse(
    List<ReferrerStats> referrers,
    long totalClicks
) {
    public record ReferrerStats(
        String domain,
        long clicks,
        double percentage
    ) {}
}
