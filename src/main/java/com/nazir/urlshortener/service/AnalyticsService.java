package com.nazir.urlshortener.service;

import com.nazir.urlshortener.domain.ShortUrl;
import com.nazir.urlshortener.domain.enums.Granularity;
import com.nazir.urlshortener.dto.response.*;
import com.nazir.urlshortener.exception.ShortUrlNotFoundException;
import com.nazir.urlshortener.repository.ClickEventRepository;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    /**
     * Whitelist of column names allowed in dynamic SQL.
     * Prevents SQL injection when building GROUP BY queries.
     */
    private static final Set<String> ALLOWED_COLUMNS = Set.of(
        "country", "device_type", "referrer_domain", "browser_name", "os_name"
    );

    private final ShortUrlRepository shortUrlRepository;
    private final ClickEventRepository clickEventRepository;
    private final JdbcTemplate jdbcTemplate;

    public AnalyticsService(ShortUrlRepository shortUrlRepository,
                            ClickEventRepository clickEventRepository,
                            JdbcTemplate jdbcTemplate) {
        this.shortUrlRepository = shortUrlRepository;
        this.clickEventRepository = clickEventRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ═══════════════════════════════════════════════════════
    //  SUMMARY
    // ═══════════════════════════════════════════════════════

    public AnalyticsSummaryResponse getSummary(String slug, Instant from, Instant to) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        UUID urlId = shortUrl.getId();

        long totalClicks   = clickEventRepository.countByShortUrlIdAndDateRange(urlId, from, to);
        long uniqueVisitors = clickEventRepository.countUniqueVisitors(urlId, from, to);

        Instant now          = Instant.now();
        Instant startOfToday = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant startOfWeek  = LocalDate.now().minusDays(7).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant startOfMonth = LocalDate.now().minusDays(30).atStartOfDay().toInstant(ZoneOffset.UTC);

        long clicksToday     = clickEventRepository.countByShortUrlIdAndDateRange(urlId, startOfToday, now);
        long clicksThisWeek  = clickEventRepository.countByShortUrlIdAndDateRange(urlId, startOfWeek, now);
        long clicksThisMonth = clickEventRepository.countByShortUrlIdAndDateRange(urlId, startOfMonth, now);

        String topCountry  = queryTopField(urlId, from, to, "country");
        String topDevice   = queryTopField(urlId, from, to, "device_type");
        String topReferrer = queryTopField(urlId, from, to, "referrer_domain");
        String topBrowser  = queryTopField(urlId, from, to, "browser_name");

        return new AnalyticsSummaryResponse(
            totalClicks, uniqueVisitors,
            clicksToday, clicksThisWeek, clicksThisMonth,
            topCountry, topDevice,
            topReferrer != null ? topReferrer : "direct",
            topBrowser
        );
    }

    // ═══════════════════════════════════════════════════════
    //  TIME SERIES
    // ═══════════════════════════════════════════════════════

    public TimeSeriesResponse getTimeSeries(String slug, Instant from, Instant to,
                                            Granularity granularity) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        UUID urlId = shortUrl.getId();

        // granularity.toPgInterval() is safe — comes from validated enum, not user input
        String sql = """
                SELECT date_trunc('%s', clicked_at) AS ts,
                       COUNT(*)                     AS clicks,
                       COUNT(DISTINCT ip_address)   AS unique_visitors
                FROM click_events
                WHERE short_url_id = ? AND clicked_at BETWEEN ? AND ?
                GROUP BY 1
                ORDER BY 1
                """.formatted(granularity.toPgInterval());

        List<TimeSeriesResponse.DataPoint> data = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new TimeSeriesResponse.DataPoint(
                rs.getTimestamp("ts").toLocalDateTime(),
                rs.getLong("clicks"),
                rs.getLong("unique_visitors")
            ),
            urlId, Timestamp.from(from), Timestamp.from(to)
        );

        long totalClicks = data.stream().mapToLong(TimeSeriesResponse.DataPoint::clicks).sum();
        return new TimeSeriesResponse(data, granularity.name(), totalClicks);
    }

    // ═══════════════════════════════════════════════════════
    //  GEO
    // ═══════════════════════════════════════════════════════

    public GeoAnalyticsResponse getGeoAnalytics(String slug, Instant from, Instant to, int limit) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        UUID urlId = shortUrl.getId();
        long totalClicks = clickEventRepository.countByShortUrlIdAndDateRange(urlId, from, to);

        // Countries
        String countrySql = """
                SELECT country AS code, COUNT(*) AS clicks
                FROM click_events
                WHERE short_url_id = ? AND clicked_at BETWEEN ? AND ?
                  AND country IS NOT NULL
                GROUP BY country
                ORDER BY clicks DESC
                LIMIT ?
                """;

        List<GeoAnalyticsResponse.CountryStats> countries = jdbcTemplate.query(
            countrySql,
            (rs, rowNum) -> {
                String code = rs.getString("code");
                long clicks = rs.getLong("clicks");
                double pct = totalClicks > 0 ? (clicks * 100.0 / totalClicks) : 0;
                return new GeoAnalyticsResponse.CountryStats(code, countryName(code), clicks, round(pct));
            },
            urlId, Timestamp.from(from), Timestamp.from(to), limit
        );

        // Cities (top 20)
        String citySql = """
                SELECT city, country, COUNT(*) AS clicks
                FROM click_events
                WHERE short_url_id = ? AND clicked_at BETWEEN ? AND ?
                  AND city IS NOT NULL
                GROUP BY city, country
                ORDER BY clicks DESC
                LIMIT 20
                """;

        List<GeoAnalyticsResponse.CityStats> cities = jdbcTemplate.query(
            citySql,
            (rs, rowNum) -> new GeoAnalyticsResponse.CityStats(
                rs.getString("city"),
                rs.getString("country"),
                rs.getLong("clicks")
            ),
            urlId, Timestamp.from(from), Timestamp.from(to)
        );

        return new GeoAnalyticsResponse(countries, cities, totalClicks);
    }

    // ═══════════════════════════════════════════════════════
    //  DEVICES
    // ═══════════════════════════════════════════════════════

    public DeviceAnalyticsResponse getDeviceAnalytics(String slug, Instant from, Instant to) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        UUID urlId = shortUrl.getId();
        long totalClicks = clickEventRepository.countByShortUrlIdAndDateRange(urlId, from, to);

        List<DeviceAnalyticsResponse.CategoryStats> deviceTypes      = queryCategoryStats(urlId, from, to, "device_type", totalClicks);
        List<DeviceAnalyticsResponse.CategoryStats> browsers         = queryCategoryStats(urlId, from, to, "browser_name", totalClicks);
        List<DeviceAnalyticsResponse.CategoryStats> operatingSystems = queryCategoryStats(urlId, from, to, "os_name", totalClicks);

        return new DeviceAnalyticsResponse(deviceTypes, browsers, operatingSystems);
    }

    // ═══════════════════════════════════════════════════════
    //  REFERRERS
    // ═══════════════════════════════════════════════════════

    public ReferrerAnalyticsResponse getReferrerAnalytics(String slug, Instant from, Instant to,
                                                          int limit) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);
        UUID urlId = shortUrl.getId();
        long totalClicks = clickEventRepository.countByShortUrlIdAndDateRange(urlId, from, to);

        String sql = """
                SELECT COALESCE(referrer_domain, 'direct') AS domain,
                       COUNT(*)                            AS clicks
                FROM click_events
                WHERE short_url_id = ? AND clicked_at BETWEEN ? AND ?
                GROUP BY referrer_domain
                ORDER BY clicks DESC
                LIMIT ?
                """;

        List<ReferrerAnalyticsResponse.ReferrerStats> referrers = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                long clicks = rs.getLong("clicks");
                double pct = totalClicks > 0 ? (clicks * 100.0 / totalClicks) : 0;
                return new ReferrerAnalyticsResponse.ReferrerStats(rs.getString("domain"), clicks, round(pct));
            },
            urlId, Timestamp.from(from), Timestamp.from(to), limit
        );

        return new ReferrerAnalyticsResponse(referrers, totalClicks);
    }

    // ═══════════════════════════════════════════════════════
    //  RAW CLICKS (paginated)
    // ═══════════════════════════════════════════════════════

    public Page<ClickDetailResponse> getRawClicks(String slug, Pageable pageable) {
        ShortUrl shortUrl = findBySlugOrThrow(slug);

        return clickEventRepository
            .findByShortUrlIdOrderByClickedAtDesc(shortUrl.getId(), pageable)
            .map(ce -> new ClickDetailResponse(
                ce.getId(),
                ce.getClickedAt(),
                ce.getIpAddress(),
                ce.getCountry(),
                ce.getCity(),
                ce.getDeviceType(),
                ce.getOsName(),
                ce.getBrowserName(),
                ce.getReferrerDomain(),
                ce.getLanguage()
            ));
    }

    // ═══════════════════════════════════════════════════════
    //  Private Helpers
    // ═══════════════════════════════════════════════════════

    private ShortUrl findBySlugOrThrow(String slug) {
        return shortUrlRepository.findBySlug(slug)
            .orElseThrow(() -> new ShortUrlNotFoundException(
                "Short URL not found: " + slug));
    }

    /**
     * Queries the most common value for a column within a date range.
     * Column name is validated against whitelist to prevent SQL injection.
     */
    private String queryTopField(UUID urlId, Instant from, Instant to, String columnName) {
        if (!ALLOWED_COLUMNS.contains(columnName)) {
            throw new IllegalArgumentException("Invalid column: " + columnName);
        }

        String sql = """
                SELECT %s AS val FROM click_events
                WHERE short_url_id = ? AND clicked_at BETWEEN ? AND ? AND %s IS NOT NULL
                GROUP BY %s ORDER BY COUNT(*) DESC LIMIT 1
                """.formatted(columnName, columnName, columnName);

        List<String> result = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> rs.getString("val"),
            urlId, Timestamp.from(from), Timestamp.from(to)
        );

        return result.isEmpty() ? null : result.getFirst();
    }

    /**
     * Generic grouped count query — used for device_type, browser_name, os_name.
     */
    private List<DeviceAnalyticsResponse.CategoryStats> queryCategoryStats(UUID urlId, Instant from, Instant to,
                                                                           String columnName, long totalClicks) {
        if (!ALLOWED_COLUMNS.contains(columnName)) {
            throw new IllegalArgumentException("Invalid column: " + columnName);
        }

        String sql = """
                SELECT COALESCE(%s, 'Unknown') AS name, COUNT(*) AS clicks
                FROM click_events
                WHERE short_url_id = ? AND clicked_at BETWEEN ? AND ?
                GROUP BY %s
                ORDER BY clicks DESC
                """.formatted(columnName, columnName);

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> {
                long clicks = rs.getLong("clicks");
                double pct = totalClicks > 0 ? (clicks * 100.0 / totalClicks) : 0;
                return new DeviceAnalyticsResponse.CategoryStats(rs.getString("name"), clicks, round(pct));
            },
            urlId, Timestamp.from(from), Timestamp.from(to)
        );
    }

    /**
     * ISO 3166-1 alpha-2 → display name.
     * "US" → "United States", "GB" → "United Kingdom"
     */
    private String countryName(String code) {
        if (code == null) return "Unknown";
        return new Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.ENGLISH);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
