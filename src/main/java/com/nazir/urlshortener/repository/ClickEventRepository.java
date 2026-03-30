package com.nazir.urlshortener.repository;

import com.nazir.urlshortener.domain.ClickEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    Page<ClickEvent> findByShortUrlIdOrderByClickedAtDesc(UUID shortUrlId, Pageable pageable);

    // ═══ Count queries with date range ═══

    @Query("""
            SELECT COUNT(e) FROM ClickEvent e
            WHERE e.shortUrlId = :id
              AND e.clickedAt BETWEEN :from AND :to
            """)
    long countByShortUrlIdAndDateRange(
        @Param("id") UUID shortUrlId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query(value = """
            SELECT COUNT(DISTINCT ip_address) FROM click_events
            WHERE short_url_id = :id
              AND clicked_at BETWEEN :from AND :to
            """,
        nativeQuery = true)
    long countUniqueVisitors(
        @Param("id") UUID shortUrlId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    // ═══ Daily Stats Aggregation Queries ═══

    @Query(value = """
            SELECT DISTINCT short_url_id FROM click_events
            WHERE clicked_at::date = :date
            """,
        nativeQuery = true)
    List<UUID> findDistinctShortUrlIdsForDate(@Param("date") LocalDate date);

    @Query(value = """
            SELECT COUNT(*) FROM click_events
            WHERE short_url_id = :id AND clicked_at::date = :date
            """,
        nativeQuery = true)
    int countClicksForDate(@Param("id") UUID shortUrlId, @Param("date") LocalDate date);

    @Query(value = """
            SELECT COUNT(DISTINCT ip_address) FROM click_events
            WHERE short_url_id = :id AND clicked_at::date = :date
            """,
        nativeQuery = true)
    int countUniqueVisitorsForDate(@Param("id") UUID shortUrlId, @Param("date") LocalDate date);

    @Query(value = """
            SELECT country FROM click_events
            WHERE short_url_id = :id AND clicked_at::date = :date AND country IS NOT NULL
            GROUP BY country ORDER BY COUNT(*) DESC LIMIT 1
            """,
        nativeQuery = true)
    String findTopCountryForDate(@Param("id") UUID shortUrlId, @Param("date") LocalDate date);

    @Query(value = """
            SELECT device_type FROM click_events
            WHERE short_url_id = :id AND clicked_at::date = :date AND device_type IS NOT NULL
            GROUP BY device_type ORDER BY COUNT(*) DESC LIMIT 1
            """,
        nativeQuery = true)
    String findTopDeviceForDate(@Param("id") UUID shortUrlId, @Param("date") LocalDate date);

    @Query(value = """
            SELECT referrer_domain FROM click_events
            WHERE short_url_id = :id AND clicked_at::date = :date AND referrer_domain IS NOT NULL
            GROUP BY referrer_domain ORDER BY COUNT(*) DESC LIMIT 1
            """,
        nativeQuery = true)
    String findTopReferrerForDate(@Param("id") UUID shortUrlId, @Param("date") LocalDate date);
}
