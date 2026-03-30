package com.nazir.urlshortener.dto.response;

import java.util.List;

public record GeoAnalyticsResponse(
    List<CountryStats> countries,
    List<CityStats> cities,
    long totalClicks
) {
    public record CountryStats(
        String code,
        String name,
        long clicks,
        double percentage
    ) {}

    public record CityStats(
        String city,
        String country,
        long clicks
    ) {}
}
