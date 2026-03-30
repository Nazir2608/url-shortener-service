package com.nazir.urlshortener.dto.response;

import java.util.List;

public record DeviceAnalyticsResponse(
    List<CategoryStats> deviceTypes,
    List<CategoryStats> browsers,
    List<CategoryStats> operatingSystems
) {
    public record CategoryStats(
        String name,
        long clicks,
        double percentage
    ) {}
}
