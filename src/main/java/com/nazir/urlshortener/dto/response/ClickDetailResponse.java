package com.nazir.urlshortener.dto.response;

import java.time.Instant;

public record ClickDetailResponse(
    Long id,
    Instant clickedAt,
    String ipAddress,
    String country,
    String city,
    String deviceType,
    String osName,
    String browserName,
    String referrerDomain,
    String language
) {}
