package com.nazir.urlshortener.domain;

import com.nazir.urlshortener.domain.ShortUrl;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Records a single redirect click with all enriched metadata.
 * Immutable after creation — no setters exposed.
 * Uses Builder pattern for clean construction.
 */
@Entity
@Table(name = "click_events")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url_id", nullable = false)
    private UUID shortUrlId;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    // ── Network ──────────────────────────────────

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "language", length = 10)
    private String language;

    // ── Geo (MaxMind) ────────────────────────────

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    // ── Device (Yauaa) ───────────────────────────

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(name = "os_name", length = 50)
    private String osName;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "browser_name", length = 50)
    private String browserName;

    @Column(name = "browser_version", length = 20)
    private String browserVersion;

    // ── Referrer ─────────────────────────────────

    @Column(name = "referrer", columnDefinition = "TEXT")
    private String referrer;

    @Column(name = "referrer_domain", length = 255)
    private String referrerDomain;

    // ── Raw ──────────────────────────────────────

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    // ── Relationship (lazy, read-only) ───────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", insertable = false, updatable = false)
    private ShortUrl shortUrl;

    // ═══════════════════════════════════════════════
    // Constructors
    // ═══════════════════════════════════════════════

    protected ClickEvent() {
        // JPA requires no-arg constructor
    }

    private ClickEvent(Builder builder) {
        this.shortUrlId = builder.shortUrlId;
        this.clickedAt = builder.clickedAt != null ? builder.clickedAt : Instant.now();
        this.ipAddress = builder.ipAddress;
        this.language = builder.language;
        this.country = builder.country;
        this.city = builder.city;
        this.region = builder.region;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.deviceType = builder.deviceType;
        this.osName = builder.osName;
        this.osVersion = builder.osVersion;
        this.browserName = builder.browserName;
        this.browserVersion = builder.browserVersion;
        this.referrer = builder.referrer;
        this.referrerDomain = builder.referrerDomain;
        this.userAgent = builder.userAgent;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ═══════════════════════════════════════════════
    // Builder (fluent)
    // ═══════════════════════════════════════════════

    public static class Builder {
        private UUID shortUrlId;
        private Instant clickedAt;
        private String ipAddress;
        private String language;
        private String country;
        private String city;
        private String region;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String deviceType;
        private String osName;
        private String osVersion;
        private String browserName;
        private String browserVersion;
        private String referrer;
        private String referrerDomain;
        private String userAgent;

        public Builder shortUrlId(UUID val)       { this.shortUrlId = val;      return this; }
        public Builder clickedAt(Instant val)     { this.clickedAt = val;       return this; }
        public Builder ipAddress(String val)      { this.ipAddress = val;       return this; }
        public Builder language(String val)       { this.language = val;        return this; }
        public Builder country(String val)        { this.country = val;         return this; }
        public Builder city(String val)           { this.city = val;            return this; }
        public Builder region(String val)         { this.region = val;          return this; }
        public Builder latitude(BigDecimal val)   { this.latitude = val;        return this; }
        public Builder longitude(BigDecimal val)  { this.longitude = val;       return this; }
        public Builder deviceType(String val)     { this.deviceType = val;      return this; }
        public Builder osName(String val)         { this.osName = val;          return this; }
        public Builder osVersion(String val)      { this.osVersion = val;       return this; }
        public Builder browserName(String val)    { this.browserName = val;     return this; }
        public Builder browserVersion(String val) { this.browserVersion = val;  return this; }
        public Builder referrer(String val)       { this.referrer = val;        return this; }
        public Builder referrerDomain(String val) { this.referrerDomain = val;  return this; }
        public Builder userAgent(String val)      { this.userAgent = val;       return this; }

        public ClickEvent build() {
            if (shortUrlId == null) {
                throw new IllegalArgumentException("shortUrlId is required");
            }
            return new ClickEvent(this);
        }
    }

    // ═══════════════════════════════════════════════
    // Getters only — entity is immutable after creation
    // ═══════════════════════════════════════════════

    public Long getId()               { return id; }
    public UUID getShortUrlId()       { return shortUrlId; }
    public Instant getClickedAt()     { return clickedAt; }
    public String getIpAddress()      { return ipAddress; }
    public String getLanguage()       { return language; }
    public String getCountry()        { return country; }
    public String getCity()           { return city; }
    public String getRegion()         { return region; }
    public BigDecimal getLatitude()   { return latitude; }
    public BigDecimal getLongitude()   { return longitude; }
    public String getDeviceType()     { return deviceType; }
    public String getOsName()         { return osName; }
    public String getOsVersion()      { return osVersion; }
    public String getBrowserName()    { return browserName; }
    public String getBrowserVersion() { return browserVersion; }
    public String getReferrer()       { return referrer; }
    public String getReferrerDomain() { return referrerDomain; }
    public String getUserAgent()      { return userAgent; }
    public ShortUrl getShortUrl()     { return shortUrl; }
}
