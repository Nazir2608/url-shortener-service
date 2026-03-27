package com.nazir.urlshortener.repository;

import com.nazir.urlshortener.domain.ClickEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByShortUrlId(UUID shortUrlId);

    Page<ClickEvent> findByShortUrlIdOrderByClickedAtDesc(UUID shortUrlId, Pageable pageable);

    List<ClickEvent> findByShortUrlIdAndClickedAtBetween(
        UUID shortUrlId, LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT c.country, COUNT(c) as cnt
        FROM ClickEvent c
        WHERE c.shortUrl.id = :urlId
          AND c.clickedAt BETWEEN :start AND :end
        GROUP BY c.country
        ORDER BY cnt DESC
        """)
    List<Object[]> countByCountry(@Param("urlId") UUID urlId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT c.deviceType, COUNT(c) as cnt
        FROM ClickEvent c
        WHERE c.shortUrl.id = :urlId
          AND c.clickedAt BETWEEN :start AND :end
        GROUP BY c.deviceType
        ORDER BY cnt DESC
        """)
    List<Object[]> countByDeviceType(@Param("urlId") UUID urlId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT c.referrerDomain, COUNT(c) as cnt
        FROM ClickEvent c
        WHERE c.shortUrl.id = :urlId
          AND c.clickedAt BETWEEN :start AND :end
        GROUP BY c.referrerDomain
        ORDER BY cnt DESC
        """)
    List<Object[]> countByReferrerDomain(@Param("urlId") UUID urlId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT c.browserName, COUNT(c) as cnt
        FROM ClickEvent c
        WHERE c.shortUrl.id = :urlId
          AND c.clickedAt BETWEEN :start AND :end
        GROUP BY c.browserName
        ORDER BY cnt DESC
        """)
    List<Object[]> countByBrowser(@Param("urlId") UUID urlId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT CAST(c.clickedAt AS LocalDate), COUNT(c)
        FROM ClickEvent c
        WHERE c.shortUrl.id = :urlId
          AND c.clickedAt BETWEEN :start AND :end
        GROUP BY CAST(c.clickedAt AS LocalDate)
        ORDER BY CAST(c.clickedAt AS LocalDate)
        """)
    List<Object[]> countDailyClicks(@Param("urlId") UUID urlId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
        SELECT COUNT(DISTINCT c.ipAddress)
        FROM ClickEvent c
        WHERE c.shortUrl.id = :urlId
          AND c.clickedAt BETWEEN :start AND :end
        """)
    long countUniqueVisitors(@Param("urlId") UUID urlId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
