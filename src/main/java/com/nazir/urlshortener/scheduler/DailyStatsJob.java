package com.nazir.urlshortener.scheduler;

import com.nazir.urlshortener.service.DailyStatsAggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Runs daily at 00:05 UTC to aggregate yesterday's clicks into daily_stats.
 */
@Component
public class DailyStatsJob {

    private static final Logger log = LoggerFactory.getLogger(DailyStatsJob.class);

    private final DailyStatsAggregatorService aggregatorService;

    public DailyStatsJob(DailyStatsAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "UTC")
    public void run() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("DailyStatsJob triggered — aggregating for {}", yesterday);

        try {
            aggregatorService.aggregateForDate(yesterday);
        } catch (Exception e) {
            log.error("DailyStatsJob failed for {}: {}", yesterday, e.getMessage(), e);
        }
    }
}
