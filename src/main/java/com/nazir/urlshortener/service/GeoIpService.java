package com.nazir.urlshortener.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.nazir.urlshortener.util.IpAddressExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;

@Service
public class GeoIpService {

    private static final Logger log = LoggerFactory.getLogger(GeoIpService.class);

    private final DatabaseReader databaseReader;

    public GeoIpService(@Nullable DatabaseReader databaseReader) {
        this.databaseReader = databaseReader;
    }

    /**
     * Looks up geographic info for an IP address.
     * Returns GeoIpResult.empty() if unavailable or lookup fails.
     * Never throws exceptions — safe for fire-and-forget usage.
     */
    public GeoIpResult lookup(String ipAddress) {
        if (databaseReader == null) {
            return GeoIpResult.empty();
        }

        if (ipAddress == null || ipAddress.isBlank() || IpAddressExtractor.isPrivateIp(ipAddress)) {
            log.debug("Skipping GeoIP lookup for private/empty IP: {}", ipAddress);
            return GeoIpResult.empty();
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse response = databaseReader.city(inetAddress);

            String countryCode = response.getCountry() != null
                ? response.getCountry().getIsoCode() : null;
            String city = response.getCity() != null
                ? response.getCity().getName() : null;
            String region = response.getMostSpecificSubdivision() != null
                ? response.getMostSpecificSubdivision().getName() : null;

            BigDecimal latitude = null;
            BigDecimal longitude = null;
            if (response.getLocation() != null) {
                if (response.getLocation().getLatitude() != null) {
                    latitude = BigDecimal.valueOf(response.getLocation().getLatitude());
                }
                if (response.getLocation().getLongitude() != null) {
                    longitude = BigDecimal.valueOf(response.getLocation().getLongitude());
                }
            }

            return new GeoIpResult(countryCode, city, region, latitude, longitude);

        } catch (GeoIp2Exception e) {
            log.debug("GeoIP no result for IP '{}': {}", ipAddress, e.getMessage());
            return GeoIpResult.empty();
        } catch (IOException e) {
            log.warn("GeoIP I/O error for IP '{}': {}", ipAddress, e.getMessage());
            return GeoIpResult.empty();
        }
    }

    public boolean isAvailable() {
        return databaseReader != null;
    }

    // ═══════════════════════════════════════════
    // Result Record
    // ═══════════════════════════════════════════

    public record GeoIpResult(
        String countryCode,
        String city,
        String region,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        public static GeoIpResult empty() {
            return new GeoIpResult(null, null, null, null, null);
        }

        public boolean isEmpty() {
            return countryCode == null && city == null;
        }
    }
}
