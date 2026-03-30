package com.nazir.urlshortener.controller;
import com.nazir.urlshortener.domain.enums.Granularity;
import com.nazir.urlshortener.dto.response.*;
import com.nazir.urlshortener.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/v1/urls/{slug}/analytics")
@Tag(name = "Analytics", description = "Click analytics for shortened URLs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // ═══ SUMMARY ═══

    @GetMapping("/summary")
    @Operation(summary = "Get analytics summary",
        description = "Total clicks, unique visitors, top country/device/referrer/browser")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(
        @PathVariable String slug,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Parameter(description = "Start date (default: 30 days ago)") LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Parameter(description = "End date (default: today)") LocalDate to
    ) {
        DateRange range = resolveDateRange(from, to);
        return ResponseEntity.ok(analyticsService.getSummary(slug, range.from(), range.to()));
    }

    // ═══ TIME SERIES ═══

    @GetMapping("/timeseries")
    @Operation(summary = "Get clicks over time",
        description = "Time series data with configurable granularity: HOUR, DAY, WEEK, MONTH")
    public ResponseEntity<TimeSeriesResponse> getTimeSeries(
        @PathVariable String slug,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "DAY")
        @Parameter(description = "HOUR, DAY, WEEK, or MONTH") Granularity granularity
    ) {
        DateRange range = resolveDateRange(from, to);
        return ResponseEntity.ok(
            analyticsService.getTimeSeries(slug, range.from(), range.to(), granularity));
    }

    // ═══ GEO ═══

    @GetMapping("/geo")
    @Operation(summary = "Get geographic analytics",
        description = "Click distribution by country and city")
    public ResponseEntity<GeoAnalyticsResponse> getGeoAnalytics(
        @PathVariable String slug,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "10") int limit
    ) {
        DateRange range = resolveDateRange(from, to);
        return ResponseEntity.ok(
            analyticsService.getGeoAnalytics(slug, range.from(), range.to(), limit));
    }

    // ═══ DEVICES ═══

    @GetMapping("/devices")
    @Operation(summary = "Get device analytics",
        description = "Breakdown by device type, browser, and OS")
    public ResponseEntity<DeviceAnalyticsResponse> getDeviceAnalytics(
        @PathVariable String slug,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        DateRange range = resolveDateRange(from, to);
        return ResponseEntity.ok(
            analyticsService.getDeviceAnalytics(slug, range.from(), range.to()));
    }

    // ═══ REFERRERS ═══

    @GetMapping("/referrers")
    @Operation(summary = "Get referrer analytics",
        description = "Click sources by referring domain")
    public ResponseEntity<ReferrerAnalyticsResponse> getReferrerAnalytics(
        @PathVariable String slug,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "10") int limit
    ) {
        DateRange range = resolveDateRange(from, to);
        return ResponseEntity.ok(
            analyticsService.getReferrerAnalytics(slug, range.from(), range.to(), limit));
    }

    // ═══ RAW CLICKS ═══

    @GetMapping("/clicks")
    @Operation(summary = "Get raw click log",
        description = "Paginated list of individual click events")
    public ResponseEntity<Page<ClickDetailResponse>> getRawClicks(
        @PathVariable String slug,
        @PageableDefault(size = 50) Pageable pageable
    ) {
        return ResponseEntity.ok(analyticsService.getRawClicks(slug, pageable));
    }

    // ═══ Helper ═══

    private DateRange resolveDateRange(LocalDate from, LocalDate to) {
        LocalDate effectiveTo   = (to != null) ? to : LocalDate.now();
        LocalDate effectiveFrom = (from != null) ? from : effectiveTo.minusDays(30);

        // Cap max range to 365 days
        if (effectiveFrom.isBefore(effectiveTo.minusDays(365))) {
            effectiveFrom = effectiveTo.minusDays(365);
        }

        return new DateRange(
            effectiveFrom.atStartOfDay().toInstant(ZoneOffset.UTC),
            effectiveTo.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        );
    }

    private record DateRange(Instant from, Instant to) {}
}
