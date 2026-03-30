package com.nazir.urlshortener.service;

import com.nazir.urlshortener.domain.DailyStat;
import com.nazir.urlshortener.repository.ClickEventRepository;
import com.nazir.urlshortener.repository.DailyStatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class DailyStatsAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(DailyStatsAggregatorService.class);

    private final ClickEventRepository clickEventRepository;
    private final DailyStatRepository dailyStatRepository;

    public DailyStatsAggregatorService(ClickEventRepository clickEventRepository,
                                       DailyStatRepository dailyStatRepository) {
        this.clickEventRepository = clickEventRepository;
        this.dailyStatRepository = dailyStatRepository;
    }

    /**
     * Aggregates click_events for a given date into daily_stats.
     * Uses UPSERT logic — safe to run multiple times for the same date.
     */
    @Transactional
    public void aggregateForDate(LocalDate date) {
        log.info("Starting daily stats aggregation for {}", date);

        List<UUID> urlIds = clickEventRepository.findDistinctShortUrlIdsForDate(date);

        if (urlIds.isEmpty()) {
            log.info("No click events found for {}", date);
            return;
        }

        int count = 0;
        for (UUID urlId : urlIds) {
            int clicks         = clickEventRepository.countClicksForDate(urlId, date);
            int uniqueVisitors = clickEventRepository.countUniqueVisitorsForDate(urlId, date);
            String topCountry  = clickEventRepository.findTopCountryForDate(urlId, date);
            String topDevice   = clickEventRepository.findTopDeviceForDate(urlId, date);
            String topReferrer = clickEventRepository.findTopReferrerForDate(urlId, date);

            // Upsert
            DailyStat stat = dailyStatRepository.findByShortUrlIdAndDate(urlId, date)
                .orElseGet(() -> new DailyStat(urlId, date, 0, 0, null, null, null));

            stat.setClickCount(clicks);
            stat.setUniqueVisitors(uniqueVisitors);
            stat.setTopCountry(topCountry);
            stat.setTopDevice(topDevice);
            stat.setTopReferrer(topReferrer);

            dailyStatRepository.save(stat);
            count++;
        }

        log.info("Aggregation complete: {} URLs processed for {}", count, date);
    }
}
