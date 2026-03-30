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

    Optional<DailyStat> findByShortUrlIdAndDate(UUID shortUrlId, LocalDate date);

    List<DailyStat> findByShortUrlIdAndDateBetweenOrderByDateAsc(
        UUID shortUrlId, LocalDate from, LocalDate to
    );

    void deleteByDateBefore(LocalDate date);
}
