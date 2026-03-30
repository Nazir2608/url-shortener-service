package com.nazir.urlshortener.service;

import com.nazir.urlshortener.domain.enums.DeviceType;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class DeviceDetectorService {

    private static final Logger log = LoggerFactory.getLogger(DeviceDetectorService.class);

    private UserAgentAnalyzer analyzer;

    /**
     * Initializes Yauaa analyzer.
     * Loads ~100MB of pattern data — takes 3-5 seconds on startup.
     * The analyzer instance is thread-safe and reusable.
     */
    @PostConstruct
    void init() {
        log.info("Initializing User-Agent analyzer (this may take a few seconds)...");

        this.analyzer = UserAgentAnalyzer.newBuilder()
            .hideMatcherLoadStats()
            .withCache(25_000)
            .withField(UserAgent.DEVICE_CLASS)
            .withField(UserAgent.OPERATING_SYSTEM_NAME)
            .withField(UserAgent.OPERATING_SYSTEM_VERSION)
            .withField(UserAgent.AGENT_NAME)
            .withField(UserAgent.AGENT_VERSION)
            .build();

        log.info("User-Agent analyzer initialized successfully");
    }

    /**
     * Parses a User-Agent string into structured device information.
     * Never throws — returns DeviceInfo.unknown() on failure.
     */
    public DeviceInfo parse(String userAgentString) {
        if (userAgentString == null || userAgentString.isBlank()) {
            return DeviceInfo.unknown();
        }

        try {
            UserAgent agent = analyzer.parse(userAgentString);

            String deviceClass = agent.getValue(UserAgent.DEVICE_CLASS);
            String osName = agent.getValue(UserAgent.OPERATING_SYSTEM_NAME);
            String osVersion = agent.getValue(UserAgent.OPERATING_SYSTEM_VERSION);
            String browserName = agent.getValue(UserAgent.AGENT_NAME);
            String browserVersion = agent.getValue(UserAgent.AGENT_VERSION);

            DeviceType deviceType = DeviceType.fromYauaaDeviceClass(deviceClass);

            return new DeviceInfo(
                deviceType,
                cleanValue(osName),
                cleanValue(osVersion),
                cleanValue(browserName),
                cleanValue(browserVersion)
            );

        } catch (Exception e) {
            log.warn("UA parse failed for '{}': {}", truncate(userAgentString, 80), e.getMessage());
            return DeviceInfo.unknown();
        }
    }

    private String cleanValue(String value) {
        if (value == null || "Unknown".equalsIgnoreCase(value) || "??".equals(value)) {
            return null;
        }
        return value;
    }

    private String truncate(String s, int maxLen) {
        return (s != null && s.length() > maxLen) ? s.substring(0, maxLen) + "..." : s;
    }

    // ═══ Result Record ═══

    public record DeviceInfo(
        DeviceType deviceType,
        String osName,
        String osVersion,
        String browserName,
        String browserVersion
    ) {
        public static DeviceInfo unknown() {
            return new DeviceInfo(DeviceType.UNKNOWN, null, null, null, null);
        }
    }
}
