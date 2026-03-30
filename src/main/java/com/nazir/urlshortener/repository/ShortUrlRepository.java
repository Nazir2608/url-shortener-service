package com.nazir.urlshortener.repository;

import com.nazir.urlshortener.domain.ShortUrl;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, UUID> {

    Optional<ShortUrl> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<ShortUrl> findByUserId(UUID userId, Pageable pageable);

    List<ShortUrl> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserId(UUID userId);

    @Query("SELECT s FROM ShortUrl s WHERE s.active = true AND s.expiresAt IS NOT NULL AND s.expiresAt < :now")
    List<ShortUrl> findExpiredUrls(@Param("now") LocalDateTime now);


    @Modifying
    @Transactional
    @Query("UPDATE ShortUrl s SET s.clickCount = s.clickCount + 1 WHERE s.id = :id")
    void incrementClickCount(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ShortUrl s SET s.isActive = false
        WHERE s.expiresAt IS NOT NULL
          AND s.expiresAt < :now
          AND s.isActive = true
        """)
    int deactivateExpiredUrls(@Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ShortUrl s SET s.isActive = false
        WHERE s.maxClicks IS NOT NULL
          AND s.clickCount >= s.maxClicks
          AND s.isActive = true
        """)
    int deactivateOverLimitUrls();

}
