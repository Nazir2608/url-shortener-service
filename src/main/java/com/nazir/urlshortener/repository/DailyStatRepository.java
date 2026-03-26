package com.nazir.urlshortener.repository;

import com.nazir.urlshortener.domain.DailyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyStatRepository extends JpaRepository<DailyStat, Long> {

    List<DailyStat> findByShortUrlIdAndStatDateBetween(
            UUID shortUrlId, LocalDate start, LocalDate end);

    Optional<DailyStat> findByShortUrlIdAndStatDate(UUID shortUrlId, LocalDate date);
}
