package com.nazir.urlshortener.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record TimeSeriesResponse(
    List<DataPoint> data,
    String granularity,
    long totalClicks
) {
    public record DataPoint(
        LocalDateTime timestamp,
        long clicks,
        long uniqueVisitors
    ) {
    }
}
