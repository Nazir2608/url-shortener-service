package com.nazir.urlshortener.dto.response;

public record AnalyticsSummaryResponse(
    long totalClicks,
    long uniqueVisitors,
    long clicksToday,
    long clicksThisWeek,
    long clicksThisMonth,
    String topCountry,
    String topDevice,
    String topReferrer,
    String topBrowser
) {}
