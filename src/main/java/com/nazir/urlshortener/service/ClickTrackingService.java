package com.nazir.urlshortener.service;

import com.nazir.urlshortener.domain.ClickEvent;
import com.nazir.urlshortener.event.ClickEventPayload;
import com.nazir.urlshortener.repository.ClickEventRepository;
import com.nazir.urlshortener.repository.ShortUrlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

/**
 * Orchestrates the complete click tracking pipeline:
 *   ClickEventPayload → GeoIP → UA Parse → referrer extract → DB persist
 */
@Service
public class ClickTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ClickTrackingService.class);

    private final GeoIpService geoIpService;
    private final DeviceDetectorService deviceDetectorService;
    private final ClickEventRepository clickEventRepository;
    private final ShortUrlRepository shortUrlRepository;

    public ClickTrackingService(GeoIpService geoIpService,
                                DeviceDetectorService deviceDetectorService,
                                ClickEventRepository clickEventRepository,
                                ShortUrlRepository shortUrlRepository) {
        this.geoIpService = geoIpService;
        this.deviceDetectorService = deviceDetectorService;
        this.clickEventRepository = clickEventRepository;
        this.shortUrlRepository = shortUrlRepository;
    }

    @Transactional
    public void track(ClickEventPayload payload) {

        // 1. GeoIP lookup
        GeoIpService.GeoIpResult geo = geoIpService.lookup(payload.ipAddress());

        // 2. User-Agent parsing
        DeviceDetectorService.DeviceInfo device = deviceDetectorService.parse(payload.userAgent());

        // 3. Extract referrer domain
        String referrerDomain = extractDomain(payload.referrer());

        // 4. Extract primary language
        String language = extractPrimaryLanguage(payload.acceptLanguage());

        // 5. Build and persist ClickEvent
        ClickEvent clickEvent = ClickEvent.builder()
            .shortUrlId(payload.shortUrlId())
            .clickedAt(payload.clickedAt())
            .ipAddress(payload.ipAddress())
            .language(language)
            // geo
            .country(geo.countryCode())
            .city(geo.city())
            .region(geo.region())
            .latitude(geo.latitude())
            .longitude(geo.longitude())
            // device
            .deviceType(device.deviceType().name())
            .osName(device.osName())
            .osVersion(device.osVersion())
            .browserName(device.browserName())
            .browserVersion(device.browserVersion())
            // referrer
            .referrer(payload.referrer())
            .referrerDomain(referrerDomain)
            // raw
            .userAgent(payload.userAgent())
            .build();

        clickEventRepository.save(clickEvent);

        // 6. Increment denormalized counter
        shortUrlRepository.incrementClickCount(payload.shortUrlId());

        log.debug("Click tracked: slug='{}', country='{}', device='{}', referrer='{}'",
            payload.slug(), geo.countryCode(), device.deviceType(), referrerDomain);
    }

    // ═══ Static helpers (package-private for testability) ═══

    /**
     * Extracts domain from referrer URL.
     * "https://www.twitter.com/post/123" → "twitter.com"
     */
    static String extractDomain(String referrer) {
        if (referrer == null || referrer.isBlank()) {
            return null;
        }
        try {
            String host = new URI(referrer).getHost();
            if (host == null) return null;
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host.toLowerCase();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extracts primary language from Accept-Language header.
     * "en-US,en;q=0.9,fr;q=0.8" → "en-US"
     */
    static String extractPrimaryLanguage(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return null;
        }
        String primary = acceptLanguage.split(",")[0].split(";")[0].trim();
        return primary.length() > 10 ? primary.substring(0, 10) : primary;
    }
}
