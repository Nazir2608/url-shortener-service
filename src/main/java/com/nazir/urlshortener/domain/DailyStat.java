package com.nazir.urlshortener.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_stats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"short_url_id", "stat_date"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    private ShortUrl shortUrl;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "click_count", nullable = false)
    @Builder.Default
    private int clickCount = 0;

    @Column(name = "unique_visitors", nullable = false)
    @Builder.Default
    private int uniqueVisitors = 0;

    @Column(name = "top_country", length = 2)
    private String topCountry;

    @Column(name = "top_device", length = 20)
    private String topDevice;

    @Column(name = "top_referrer")
    private String topReferrer;
}
