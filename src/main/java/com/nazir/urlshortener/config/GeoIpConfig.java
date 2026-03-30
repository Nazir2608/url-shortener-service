package com.nazir.urlshortener.config;

import com.maxmind.geoip2.DatabaseReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class GeoIpConfig {

    private static final Logger log = LoggerFactory.getLogger(GeoIpConfig.class);

    @Value("${geoip.database-path:classpath:maxmind/GeoLite2-City.mmdb}")
    private Resource geoIpDatabase;

    @Value("${geoip.enabled:true}")
    private boolean enabled;

    /**
     * Creates MaxMind DatabaseReader bean.
     * Returns null if disabled or file not found — GeoIpService handles null gracefully.
     */
    @Bean
    public DatabaseReader geoIpDatabaseReader() {
        if (!enabled) {
            log.info("GeoIP lookup disabled via configuration");
            return null;
        }

        try {
            if (!geoIpDatabase.exists()) {
                log.warn(
                    "GeoIP database not found at '{}'. Geo lookup will return empty results. " +
                        "Download GeoLite2-City.mmdb from https://dev.maxmind.com/geoip/geolite2-free-geolocation-data",
                    geoIpDatabase.getDescription()
                );
                return null;
            }

            DatabaseReader reader = new DatabaseReader.Builder(geoIpDatabase.getInputStream())
                .build();

            log.info("GeoIP database loaded successfully from '{}'", geoIpDatabase.getDescription());
            return reader;

        } catch (IOException e) {
            log.error("Failed to load GeoIP database: {}", e.getMessage());
            return null;
        }
    }
}
