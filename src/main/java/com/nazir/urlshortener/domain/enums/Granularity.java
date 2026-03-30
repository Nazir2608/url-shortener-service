package com.nazir.urlshortener.domain.enums;

/**
 * Time granularity for time-series analytics queries.
 * Each value maps to a PostgreSQL date_trunc() interval.
 */
public enum Granularity {

    HOUR("hour"),
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String pgInterval;

    Granularity(String pgInterval) {
        this.pgInterval = pgInterval;
    }

    public String toPgInterval() {
        return pgInterval;
    }
}
