package com.nazir.urlshortener.domain;

import com.nazir.urlshortener.domain.enums.DeviceType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "click_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString(exclude = "shortUrl")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    @Column(name = "clicked_at", nullable = false)
    private LocalDateTime clickedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 2)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    @Column(name = "os_name", length = 50)
    private String osName;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "browser_name", length = 50)
    private String browserName;

    @Column(name = "browser_version", length = 20)
    private String browserVersion;

    @Column(columnDefinition = "TEXT")
    private String referrer;

    @Column(name = "referrer_domain")
    private String referrerDomain;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(length = 10)
    private String language;

    @PrePersist
    protected void onCreate() {
        if (clickedAt == null) {
            clickedAt = LocalDateTime.now();
        }
    }
}
