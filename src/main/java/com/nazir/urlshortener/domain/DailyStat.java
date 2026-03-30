package com.nazir.urlshortener.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Pre-aggregated daily statistics per URL.
 * Populated nightly by DailyStatsJob.
 */
@Entity
@Table(
    name = "daily_stats",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_daily_stats_url_date",
        columnNames = {"short_url_id", "date"}
    )
)
public class DailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_url_id", nullable = false)
    private UUID shortUrlId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "click_count", nullable = false)
    private int clickCount;

    @Column(name = "unique_visitors", nullable = false)
    private int uniqueVisitors;

    @Column(name = "top_country", length = 2)
    private String topCountry;

    @Column(name = "top_device", length = 20)
    private String topDevice;

    @Column(name = "top_referrer", length = 255)
    private String topReferrer;

    // ═══ Constructors ═══

    protected DailyStat() {
        // JPA
    }

    public DailyStat(UUID shortUrlId, LocalDate date, int clickCount, int uniqueVisitors,
                     String topCountry, String topDevice, String topReferrer) {
        this.shortUrlId = shortUrlId;
        this.date = date;
        this.clickCount = clickCount;
        this.uniqueVisitors = uniqueVisitors;
        this.topCountry = topCountry;
        this.topDevice = topDevice;
        this.topReferrer = topReferrer;
    }

    // ═══ Getters ═══

    public Long getId()              { return id; }
    public UUID getShortUrlId()      { return shortUrlId; }
    public LocalDate getDate()       { return date; }
    public int getClickCount()       { return clickCount; }
    public int getUniqueVisitors()   { return uniqueVisitors; }
    public String getTopCountry()    { return topCountry; }
    public String getTopDevice()     { return topDevice; }
    public String getTopReferrer()   { return topReferrer; }

    // ═══ Setters for aggregation updates ═══

    public void setClickCount(int clickCount)           { this.clickCount = clickCount; }
    public void setUniqueVisitors(int uniqueVisitors)   { this.uniqueVisitors = uniqueVisitors; }
    public void setTopCountry(String topCountry)        { this.topCountry = topCountry; }
    public void setTopDevice(String topDevice)          { this.topDevice = topDevice; }
    public void setTopReferrer(String topReferrer)      { this.topReferrer = topReferrer; }
}
